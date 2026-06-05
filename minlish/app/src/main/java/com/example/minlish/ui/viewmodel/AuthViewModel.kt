package com.example.minlish.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.minlish.model.User
import com.example.minlish.model.Deck
import com.example.minlish.model.Vocabulary
import com.example.minlish.repository.UserRepository
import com.example.minlish.repository.DeckRepository
import com.example.minlish.repository.VocabularyRepository
import com.example.minlish.util.PasswordUtils
import com.example.minlish.util.SessionManager
import com.example.minlish.notification.InAppNotificationService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository()
    private val deckRepo = DeckRepository()
    private val vocabRepo = VocabularyRepository()
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
                createDefaultDeck(user.id) // Create default deck
                SessionManager.saveUserId(getApplication(), user.id)
                currentUserState.postValue(user)

                InAppNotificationService.sendAutomatedEmail(
                    toEmail = email,
                    subject = "Welcome to MinLish!",
                    body = "Hello $name, thank you for joining MinLish. We will help you master English vocabulary every day!"
                )
            } catch (e: Exception) {
                errorState.postValue(e.message ?: "Đăng ký thất bại")
            } finally {
                isLoadingState.postValue(false)
            }
        }
    }

    fun login(email: String, password: String) {
        android.util.Log.d("AuthDebug", "Login attempt for: $email")
        if (email.isBlank() || password.isBlank()) {
            errorState.value = "Vui lòng nhập đầy đủ thông tin"
            return
        }
        isLoadingState.value = true
        errorState.value = null

        viewModelScope.launch {
            try {
                val user = repo.findByEmail(email)
                if (user == null) {
                    android.util.Log.d("AuthDebug", "User not found")
                    errorState.postValue("Email hoặc mật khẩu sai")
                    return@launch
                }
                
                if (!PasswordUtils.verify(password, user.passwordHash)) {
                    android.util.Log.d("AuthDebug", "Password mismatch")
                    errorState.postValue("Email hoặc mật khẩu sai")
                    return@launch
                }
                
                android.util.Log.d("AuthDebug", "Login success for user: ${user.id}")
                SessionManager.saveUserId(getApplication(), user.id)
                currentUserState.postValue(user)
            } catch (e: Exception) {
                android.util.Log.e("AuthDebug", "Login error", e)
                errorState.postValue(e.message ?: "Đăng nhập thất bại")
            } finally {
                isLoadingState.postValue(false)
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        android.util.Log.d("AuthDebug", "Google login attempt")
        isGoogleLoadingState.value = true
        errorState.value = null

        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = firebaseAuth.signInWithCredential(credential).await()
                val firebaseUser = result.user ?: throw Exception("Đăng nhập Google thất bại")
                
                val email = firebaseUser.email ?: throw Exception("Không lấy được email")
                val name = firebaseUser.displayName ?: email.substringBefore("@")

                android.util.Log.d("AuthDebug", "Firebase Google success: $email")

                var user = repo.findByEmail(email)
                if (user == null) {
                    android.util.Log.d("AuthDebug", "Creating new user for Google login")
                    user = User(name = name, email = email, passwordHash = "")
                    repo.save(user)
                    createDefaultDeck(user.id) // Create default deck for new Google user
                }
                
                android.util.Log.d("AuthDebug", "Login success for Google user: ${user.id}")
                SessionManager.saveUserId(getApplication(), user.id)
                currentUserState.postValue(user)
            } catch (e: Exception) {
                android.util.Log.e("AuthDebug", "Google login error", e)
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

    fun resetPassword(email: String, newPassword: String) {
        if (newPassword.length < 6) {
            errorState.value = "Mật khẩu tối thiểu 6 ký tự"
            return
        }
        isLoadingState.value = true
        viewModelScope.launch {
            try {
                val user = repo.findByEmail(email)
                if (user == null) {
                    errorState.postValue("Người dùng không tồn tại")
                    return@launch
                }
                val updated = user.copy(passwordHash = PasswordUtils.hash(newPassword))
                repo.update(updated)
                // Clear error if success
                errorState.postValue(null)
            } catch (e: Exception) {
                errorState.postValue("Lỗi đặt lại mật khẩu: ${e.localizedMessage}")
            } finally {
                isLoadingState.postValue(false)
            }
        }
    }

    private suspend fun createDefaultDeck(userId: String) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val defaultDeck = Deck(
            name = "Welcome to MinLish! 🌟",
            description = "Start your journey with these essential words.",
            tags = "General, Starter",
            createdAt = today,
            ownerId = userId,
            newWordsPerDay = 5
        )
        deckRepo.save(defaultDeck)

        val starterWords = listOf(
            Vocabulary(
                word = "Innovative",
                pronunciation = "Innovative",
                meaning = "đổi mới",
                description = "introducing a new ideas, methods and products",
                example = "The company developed an innovative way to teach vocabulary",
                collocation = "innovative solution",
                relatedWords = "creative, modern, original",
                note = "Frequently used in business, technology, and education contexts",
                deckId = defaultDeck.id
            ),
            Vocabulary(
                word = "Persistence",
                pronunciation = "/pəˈsɪstəns/",
                meaning = "kiên trì",
                description = "continuing to do something even though it is difficult or other people oppose it",
                example = "His persistence finally paid off when he won the race",
                collocation = "dogged persistence",
                relatedWords = "perseverance, determination",
                note = "Often used to describe someone's character",
                deckId = defaultDeck.id
            ),
            Vocabulary(
                word = "Ambiguous",
                pronunciation = "/æmˈbɪɡjuəs/",
                meaning = "mơ hồ",
                description = "having more than one possible meaning; uncertain",
                example = "The instructions were ambiguous, so I asked for clarification",
                collocation = "ambiguous statement",
                relatedWords = "vague, unclear",
                note = "Common in academic and legal writing",
                deckId = defaultDeck.id
            ),
            Vocabulary(
                word = "Collaborate",
                pronunciation = "/kəˈlæbəreɪt/",
                meaning = "hợp tác",
                description = "to work with another person or group in order to achieve something",
                example = "We need to collaborate on this project to finish it on time",
                collocation = "collaborate with someone",
                relatedWords = "cooperate, join forces",
                note = "A high-frequency word in professional settings",
                deckId = defaultDeck.id
            ),
            Vocabulary(
                word = "Diligent",
                pronunciation = "/ˈdɪlɪdʒənt/",
                meaning = "siêng năng",
                description = "showing care and effort in your work or duties",
                example = "She is a diligent worker who never misses a deadline",
                collocation = "diligent student",
                relatedWords = "hard-working, industrious",
                note = "A positive adjective to describe a person",
                deckId = defaultDeck.id
            )
        )

        starterWords.forEach { vocabRepo.save(it) }
    }

    fun checkUserExists(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val user = repo.findByEmail(email)
                onResult(user != null)
            } catch (e: Exception) {
                onResult(false)
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