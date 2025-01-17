package com.ivy.wallet.ui.edit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivy.wallet.base.asLiveData
import com.ivy.wallet.base.ioThread
import com.ivy.wallet.base.timeNowUTC
import com.ivy.wallet.event.AccountsUpdatedEvent
import com.ivy.wallet.logic.AccountCreator
import com.ivy.wallet.logic.CategoryCreator
import com.ivy.wallet.logic.PaywallLogic
import com.ivy.wallet.logic.PlannedPaymentsLogic
import com.ivy.wallet.logic.currency.ExchangeRatesLogic
import com.ivy.wallet.logic.model.CreateAccountData
import com.ivy.wallet.logic.model.CreateCategoryData
import com.ivy.wallet.model.TransactionType
import com.ivy.wallet.model.entity.Account
import com.ivy.wallet.model.entity.Category
import com.ivy.wallet.model.entity.Transaction
import com.ivy.wallet.persistence.SharedPrefs
import com.ivy.wallet.persistence.dao.*
import com.ivy.wallet.sync.uploader.TransactionUploader
import com.ivy.wallet.ui.IvyContext
import com.ivy.wallet.ui.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EditTransactionViewModel @Inject constructor(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val settingsDao: SettingsDao,
    private val ivyContext: IvyContext,
    private val transactionUploader: TransactionUploader,
    private val sharedPrefs: SharedPrefs,
    private val exchangeRatesLogic: ExchangeRatesLogic,
    private val categoryCreator: CategoryCreator,
    private val accountCreator: AccountCreator,
    private val paywallLogic: PaywallLogic,
    private val plannedPaymentsLogic: PlannedPaymentsLogic
) : ViewModel() {

    private val _transactionType = MutableLiveData<TransactionType>()
    val transactionType = _transactionType

    private val _initialTitle = MutableLiveData<String?>()
    val initialTitle = _initialTitle.asLiveData()

    private val _currency = MutableLiveData<String>()
    val currency = _currency.asLiveData()

    private val _description = MutableLiveData<String?>()
    val description = _description.asLiveData()

    private val _dateTime = MutableLiveData<LocalDateTime?>()
    val dateTime = _dateTime.asLiveData()

    private val _dueDate = MutableLiveData<LocalDateTime?>()
    val dueDate = _dueDate.asLiveData()

    private val _accounts = MutableLiveData<List<Account>>()
    val accounts = _accounts.asLiveData()

    private val _categories = MutableLiveData<List<Category>>()
    val categories = _categories.asLiveData()

    private val _account = MutableLiveData<Account>()
    val account = _account.asLiveData()

    private val _toAccount = MutableLiveData<Account?>()
    val toAccount = _toAccount.asLiveData()

    private val _category = MutableLiveData<Category?>()
    val category = _category.asLiveData()

    private val _amount = MutableLiveData(0.0)
    val amount = _amount.asLiveData()

    private val _hasChanges = MutableLiveData(false)
    val hasChanges = _hasChanges.asLiveData()

    private var loadedTransaction: Transaction? = null
    private var editMode = false

    var title: String? = null

    fun start(screen: Screen.EditTransaction) {
        viewModelScope.launch {
            editMode = screen.initialTransactionId != null

            val accounts = ioThread { accountDao.findAll() }!!
            if (accounts.isEmpty()) {
                closeScreen()
                return@launch
            }
            _accounts.value = accounts

            _categories.value = ioThread { categoryDao.findAll() }!!

            reset()

            loadedTransaction = screen.initialTransactionId?.let {
                ioThread { transactionDao.findById(it)!! }
            } ?: Transaction(
                accountId = defaultAccountId(
                    screen = screen,
                    accounts = accounts
                ),
                categoryId = screen.categoryId,
                type = screen.type,
                amount = 0.0,
            )

            display(loadedTransaction!!)
        }
    }

    private suspend fun defaultAccountId(
        screen: Screen.EditTransaction,
        accounts: List<Account>,
    ): UUID {
        if (screen.accountId != null) {
            return screen.accountId
        }

        val lastSelectedId = sharedPrefs.getString(SharedPrefs.LAST_SELECTED_ACCOUNT_ID, null)
            ?.let { UUID.fromString(it) }
        if (lastSelectedId != null && ioThread { accounts.find { it.id == lastSelectedId } } != null) {
            //use last selected account
            return lastSelectedId
        }

        return accounts.first().id
    }

    private suspend fun display(transaction: Transaction) {
        this.title = transaction.title

        _transactionType.value = transaction.type
        _initialTitle.value = transaction.title
        _dateTime.value = transaction.dateTime
        _description.value = transaction.description
        _dueDate.value = transaction.dueDate
        val selectedAccount = ioThread { accountDao.findById(transaction.accountId)!! }
        _account.value = selectedAccount
        _toAccount.value = transaction.toAccountId?.let {
            ioThread { accountDao.findById(it) }
        }
        _category.value = transaction.smartCategoryId()?.let {
            ioThread { categoryDao.findById(it) }
        }
        _amount.value = transaction.amount

        updateCurrency(account = selectedAccount)
    }

    private suspend fun updateCurrency(account: Account) {
        _currency.value = account.currency ?: baseCurrency()
    }

    private suspend fun baseCurrency(): String = ioThread { settingsDao.findFirst().currency }

    fun onAmountChanged(newAmount: Double) {
        loadedTransaction = loadedTransaction().copy(
            amount = newAmount
        )
        _amount.value = newAmount

        saveIfEditMode()
    }

    fun onTitleChanged(newTitle: String?) {
        loadedTransaction = loadedTransaction().copy(
            title = newTitle
        )
        this.title = newTitle

        saveIfEditMode()
    }

    fun onDescriptionChanged(newDescription: String?) {
        loadedTransaction = loadedTransaction().copy(
            description = newDescription
        )
        _description.value = newDescription

        saveIfEditMode()
    }

    fun onCategoryChanged(newCategory: Category?) {
        loadedTransaction = loadedTransaction().copy(
            categoryId = newCategory?.id
        )
        _category.value = newCategory

        saveIfEditMode()
    }

    fun onAccountChanged(newAccount: Account) {
        loadedTransaction = loadedTransaction().copy(
            accountId = newAccount.id
        )
        _account.value = newAccount

        viewModelScope.launch {
            updateCurrency(account = newAccount)
        }

        //update last selected account
        sharedPrefs.putString(SharedPrefs.LAST_SELECTED_ACCOUNT_ID, newAccount.id.toString())

        saveIfEditMode()
    }

    fun onToAccountChanged(newAccount: Account) {
        loadedTransaction = loadedTransaction().copy(
            toAccountId = newAccount.id
        )
        _toAccount.value = newAccount

        saveIfEditMode()
    }

    fun onDueDateChanged(newDueDate: LocalDateTime?) {
        loadedTransaction = loadedTransaction().copy(
            dueDate = newDueDate
        )
        _dueDate.value = newDueDate

        saveIfEditMode()
    }

    fun onSetDateTime(newDateTime: LocalDateTime) {
        loadedTransaction = loadedTransaction().copy(
            dateTime = newDateTime
        )
        _dateTime.value = newDateTime

        saveIfEditMode()
    }

    fun onSetTransactionType(newTransactionType: TransactionType) {
        loadedTransaction = loadedTransaction().copy(
            type = newTransactionType
        )
        _transactionType.value = newTransactionType

        saveIfEditMode()
    }


    fun onPayPlannedPayment() {
        viewModelScope.launch {
            plannedPaymentsLogic.payOrGet(
                transaction = loadedTransaction(),
                syncTransaction = false
            ) { paidTransaction ->
                loadedTransaction = paidTransaction
                _dueDate.value = paidTransaction.dueDate
                _dateTime.value = paidTransaction.dateTime

                saveIfEditMode(
                    closeScreen = true
                )
            }
        }
    }


    fun delete() {
        viewModelScope.launch {
            ioThread {
                loadedTransaction?.let {
                    transactionDao.flagDeleted(it.id)
                }
                closeScreen()

                loadedTransaction?.let {
                    transactionUploader.delete(it.id)
                }
            }
        }
    }

    fun createCategory(data: CreateCategoryData) {
        viewModelScope.launch {
            categoryCreator.createCategory(data) {
                _categories.value = ioThread { categoryDao.findAll() }!!

                //Select the newly created category
                onCategoryChanged(it)
            }
        }
    }

    fun editCategory(updatedCategory: Category) {
        viewModelScope.launch {
            categoryCreator.editCategory(updatedCategory) {
                _categories.value = ioThread { categoryDao.findAll() }!!
            }
        }
    }

    fun createAccount(data: CreateAccountData) {
        viewModelScope.launch {
            accountCreator.createAccount(data) {
                EventBus.getDefault().post(AccountsUpdatedEvent())
                _accounts.value = ioThread { accountDao.findAll() }!!
            }
        }
    }

    private fun saveIfEditMode(closeScreen: Boolean = false) {
        if (editMode) {
            _hasChanges.value = true

            save(closeScreen)
        }
    }

    fun save(closeScreen: Boolean = true) {
        if (!validateTransaction()) {
            return
        }

        viewModelScope.launch {
            paywallLogic.protectQuotaExceededWithPaywall(
                onPaywallHit = {
                    ivyContext.back()
                }
            ) {
                saveInternal(closeScreen = closeScreen)
            }
        }
    }

    private suspend fun saveInternal(closeScreen: Boolean) {
        try {
            ioThread {
                val amount = amount.value ?: error("no amount")

                loadedTransaction = loadedTransaction().copy(
                    accountId = account.value?.id ?: error("no accountId"),
                    toAccountId = toAccount.value?.id,
                    toAmount = transferToAmount(amount = amount),
                    title = title?.trim(),
                    description = description.value?.trim(),
                    amount = amount,
                    type = transactionType.value ?: error("no transaction type"),
                    dueDate = dueDate.value,
                    dateTime = when {
                        loadedTransaction().dateTime == null &&
                                dueDate.value == null -> {
                            timeNowUTC()
                        }
                        else -> loadedTransaction().dateTime
                    },
                    categoryId = category.value?.id,

                    isSynced = false
                )

                transactionDao.save(loadedTransaction())
            }

            if (closeScreen) {
                closeScreen()

                ioThread {
                    transactionUploader.sync(loadedTransaction())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setHasChanges(hasChanges: Boolean) {
        _hasChanges.value = hasChanges
    }

    private suspend fun transferToAmount(
        amount: Double
    ): Double? {
        if (transactionType.value != TransactionType.TRANSFER) return null
        val toCurrency = toAccount.value?.currency ?: return null
        val fromCurrency = account.value?.currency ?: return null

        return exchangeRatesLogic.convertAmount(
            baseCurrency = baseCurrency(),
            amount = amount,
            fromCurrency = fromCurrency,
            toCurrency = toCurrency
        )
    }

    private fun closeScreen() {
        if (ivyContext.backStackEmpty()) {
            ivyContext.resetBackStack()
            ivyContext.navigateTo(Screen.Main)
        } else {
            ivyContext.back()
        }
    }

    private fun validateTransaction(): Boolean {
        if (transactionType.value == TransactionType.TRANSFER && toAccount.value == null) {
            return false
        }

        if (amount.value == 0.0) {
            return false
        }

        return true
    }

    private fun reset() {
        loadedTransaction = null

        _initialTitle.value = null
        _description.value = null
        _dueDate.value = null
        _category.value = null
        _hasChanges.value = false
    }

    private fun loadedTransaction() = loadedTransaction ?: error("Loaded transaction is null")
}