package com.example.foodmart.viewmodel



import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.foodmart.model.ProductModel
import com.example.foodmart.repository.ProductRepositoryImpl

class ProductViewModel(private val repo: ProductRepositoryImpl) : ViewModel() {

    fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        repo.uploadImage(context, imageUri, callback)
    }

    fun addProduct(model: ProductModel, callback: (Boolean, String) -> Unit) {
        repo.addProduct(model, callback)
    }

    fun deleteProduct(productID: String, callback: (Boolean, String) -> Unit) {
        repo.deleteProduct(productID, callback)
    }

    fun updateProduct(productID: String, productData: MutableMap<String, Any?>, callback: (Boolean, String) -> Unit) {
        repo.updateProduct(productID, productData, callback)
    }

    private val _products = MutableLiveData<ProductModel?>()
    val products: LiveData<ProductModel?> get() = _products

    fun getProductByID(productID: String) {
        repo.getProductByID(productID) { data, success, message ->
            if (success) {
                _products.postValue(data)
            } else {
                _products.postValue(null)
            }
        }
    }

    private val _allProducts = MutableLiveData<List<ProductModel>>()
    val allProducts: LiveData<List<ProductModel>> get() = _allProducts

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun getAllProduct() {
        _loading.postValue(true)
        repo.getAllProduct { data, success, message ->
            if (success) {
                _loading.postValue(false)
                Log.d("ShineSales", message)
                _allProducts.postValue((data ?: emptyList()) as List<ProductModel>?)
            } else {
                _loading.postValue(false)
                Log.d("ShineSales", message)
                _allProducts.postValue(emptyList())
            }
        }
    }
}