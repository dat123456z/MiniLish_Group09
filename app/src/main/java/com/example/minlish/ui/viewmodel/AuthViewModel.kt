package com.example.minlish.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.minlish.model.User
import com.example.minlish.repository.UserRepository
import com.example.minlish.util.PasswordUtils
import com.example.minlish.util.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository()
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val currentUserState = MutableLiveData<User?>(null)
    val currentUser: LiveData<User?> = currentUserState

    private val errorState = MutableLiveData<String?>(null)
    val error: LiveData<String?> = errorState

    private val isLoadingState = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = isLoadingState

    private val isGoogleLoadingState = MutableLiveData(false)
    val isGoogleLoading: LiveData<Boolean> = isGoogleLoadingState

    private val isInitializingState = MutableLiveData(true)
    val isInitializing: LiveData<Boolean> = isInitializingState

    init {
        val savedId = SessionManager.getUserId(application)
        if (savedId != null) {
            viewModelScope.launch {
                val user = repo.findById(savedId)
                currentUserState.postValue(user)
                isInitializingState.postValue(false)
            }
        } else {
            isInitializingState.value = false
        }
    }

    fun register(name: String, email: String, password: String, goal: String, level: String) {
        if (name.isBlank()) { errorState.value = "Vui lòng nhập họ tên"; return }
        if (!email.contains("@")) { errorState.value = "Email không hợp lệ"; return }
        if (password.length < 6) { errorState.value = "Mật khẩu tối thiểu 6 ký tự"; return }

        isLoadingState.value = true
        errorState.value = null

        viewModelScope.launch {
            try {
                if (repo.findByEmail(email) != null) {
                    errorState.postValue("Email đã tồn tại")
                    return@launch
                }
                val user = User(
                    name = name,
                    email = email,
                    passwordHash = PasswordUtils.hash(password),
                    goal = goal,
                    level = level
                )
                repo.save(user)
                SessionManager.saveUserId(getApplication(), user.id)
                currentUserState.postValue(user)
            } catch (e: Exception) {
                errorState.postValue(e.message ?: "Đăng ký thất bại")
            } finally {
                isLoadingState.postValue(false)
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            errorState.value = "Vui lòng nhập đầy đủ thông tin"
            return
        }
        isLoadingState.value = true
        errorState.value = null

        viewModelScope.launch {
            try {
                val user = repo.findByEmail(email)
                if (user == null || !PasswordUtils.verify(password, user.passwordHash)) {
                    errorState.postValue("Email hoặc mật khẩu sai")
                    return@launch
                }
                SessionManager.saveUserId(getApplication(), user.id)
                currentUserState.postValue(user)
            } catch (e: Exception) {
                errorState.postValue(e.message ?: "Đăng nhập thất bại")
            } finally {
                isLoadingState.postValue(false)
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        isGoogleLoadingState.value = true
        errorState.value = null

        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = firebaseAuth.signInWithCredential(credential).await()
                val firebaseUser = result.user ?: throw Exception("Đăng nhập Google thất bại")
                val email = firebaseUser.email ?: throw Exception("Không lấy được email")
                val name = firebaseUser.displayName ?: email.substringBefore("@")

                var user = repo.findByEmail(email)
                if (user == null) {
                    user = User(name = name, email = email, passwordHash = "")
                    repo.save(user)
                }
                SessionManager.saveUserId(getApplication(), user.id)
                currentUserState.postValue(user)
            } catch (e: Exception) {
                errorState.postValue(e.message ?: "Đăng nhập Google thất bại")
            } finally {
                isGoogleLoadingState.postValue(false)
            }
        }
    }

    fun updateUser(name: String, email: String, goal: String, level: String) {
        val current = currentUserState.value ?: return
        if (name.isBlank()) { errorState.value = "Tên không được để trống"; return }
        if (!email.contains("@")) { errorState.value = "Email không hợp lệ"; return }

        isLoadingState.value = true
        viewModelScope.launch {
            try {
                val existingByEmail = repo.findByEmail(email)
                if (existingByEmail != null && existingByEmail.id != current.id) {
                    errorState.postValue("Email đã được dùng bởi tài khoản khác")
                    return@launch
                }
                val updated = current.copy(name = name, email = email, goal = goal, level = level)
                repo.update(updated)
                currentUserState.postValue(updated)
            } catch (e: Exception) {
                errorState.postValue(e.message ?: "Cập nhật thất bại")
            } finally {
                isLoadingState.postValue(false)
            }
        }
    }

    fun clearError() { errorState.postValue(null) }

    fun logout() {
        firebaseAuth.signOut()
        SessionManager.clear(getApplication())
        currentUserState.postValue(null)
    }
}