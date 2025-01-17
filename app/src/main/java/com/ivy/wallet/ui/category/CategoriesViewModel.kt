package com.ivy.wallet.ui.category

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivy.wallet.base.asLiveData
import com.ivy.wallet.base.ioThread
import com.ivy.wallet.logic.CategoryCreator
import com.ivy.wallet.logic.WalletCategoryLogic
import com.ivy.wallet.logic.model.CreateCategoryData
import com.ivy.wallet.persistence.dao.CategoryDao
import com.ivy.wallet.persistence.dao.SettingsDao
import com.ivy.wallet.sync.item.CategorySync
import com.ivy.wallet.ui.IvyContext
import com.ivy.wallet.ui.onboarding.model.TimePeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryDao: CategoryDao,
    private val settingsDao: SettingsDao,
    private val categoryLogic: WalletCategoryLogic,
    private val categorySync: CategorySync,
    private val categoryCreator: CategoryCreator,
    private val ivyContext: IvyContext
) : ViewModel() {

    private val _currency = MutableLiveData<String>()
    val currency = _currency.asLiveData()

    private val _categories = MutableLiveData<List<CategoryData>>()
    val categories = _categories.asLiveData()

    fun start() {
        viewModelScope.launch {
            val range =
                TimePeriod.thisMonth().toRange(ivyContext.startDateOfMonth) //this must be monthly

            _currency.value = ioThread { settingsDao.findFirst().currency }!!

            _categories.value = ioThread {
                categoryDao
                    .findAll()
                    .map {
                        CategoryData(
                            category = it,
                            monthlyBalance = categoryLogic.calculateCategoryBalance(
                                it,
                                range
                            ),
                            monthlyIncome = categoryLogic.calculateCategoryIncome(
                                category = it,
                                range = range
                            ),
                            monthlyExpenses = categoryLogic.calculateCategoryExpenses(
                                category = it,
                                range = range
                            ),
                        )
                    }
            }!!
        }
    }

    fun reorder(newOrder: List<CategoryData>) {
        viewModelScope.launch {
            ioThread {
                newOrder.forEachIndexed { index, categoryData ->
                    categoryDao.save(
                        categoryData.category.copy(
                            orderNum = index.toDouble(),
                            isSynced = false
                        )
                    )
                }
            }
            start()

            ioThread {
                categorySync.sync()
            }
        }
    }

    fun createCategory(data: CreateCategoryData) {
        viewModelScope.launch {
            categoryCreator.createCategory(data) {
                start()
            }
        }
    }
}