package me.marukon.password.generator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var lengthSeekBar: SeekBar
    private lateinit var lengthTextView: TextView
    private lateinit var includeLettersCheckBox: CheckBox
    private lateinit var includeUppercaseCheckBox: CheckBox
    private lateinit var includeNumbersCheckBox: CheckBox
    private lateinit var includeSymbolsCheckBox: CheckBox
    private lateinit var generateButton: Button
    private lateinit var passwordTextView: TextView
    private lateinit var copyButton: Button
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: PasswordHistoryAdapter
    private val passwordHistory = mutableListOf<String>()
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val PREFS_NAME = "password_generator_prefs"
        private const val KEY_PASSWORD_HISTORY = "password_history"
        private const val MAX_HISTORY_SIZE = 20
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initSharedPreferences()
        initViews()
        setupListeners()
        loadPasswordHistory()
    }

    private fun initSharedPreferences() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun initViews() {
        lengthSeekBar = findViewById(R.id.lengthSeekBar)
        lengthTextView = findViewById(R.id.lengthTextView)
        includeLettersCheckBox = findViewById(R.id.includeLettersCheckBox)
        includeUppercaseCheckBox = findViewById(R.id.includeUppercaseCheckBox)
        includeNumbersCheckBox = findViewById(R.id.includeNumbersCheckBox)
        includeSymbolsCheckBox = findViewById(R.id.includeSymbolsCheckBox)
        generateButton = findViewById(R.id.generateButton)
        passwordTextView = findViewById(R.id.passwordTextView)
        copyButton = findViewById(R.id.copyButton)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)

        // 设置默认值
        lengthSeekBar.max = 50
        lengthSeekBar.min = 4
        lengthSeekBar.progress = 15
        lengthTextView.text = getString(R.string.password_length, 15)

        // 默认选中小写字母和数字
        includeLettersCheckBox.isChecked = true
        includeNumbersCheckBox.isChecked = true

        // 设置历史记录
        historyAdapter = PasswordHistoryAdapter(passwordHistory) { password ->
            copyPasswordToClipboard(password)
        }
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter
    }

    private fun setupListeners() {
        lengthSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                lengthTextView.text = getString(R.string.password_length, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        generateButton.setOnClickListener {
            generatePassword()
        }

        copyButton.setOnClickListener {
            copyToClipboard()
        }
    }

    private fun loadPasswordHistory() {
        val historySet = sharedPreferences.getStringSet(KEY_PASSWORD_HISTORY, emptySet()) ?: emptySet()
        passwordHistory.clear()
        passwordHistory.addAll(historySet.toList())
        historyAdapter.notifyDataSetChanged()
    }

    private fun savePasswordHistory() {
        val historySet = passwordHistory.toSet()
        sharedPreferences.edit()
            .putStringSet(KEY_PASSWORD_HISTORY, historySet)
            .apply()
    }

    private fun generatePassword() {
        val length = lengthSeekBar.progress
        val includeLetters = includeLettersCheckBox.isChecked
        val includeUppercase = includeUppercaseCheckBox.isChecked
        val includeNumbers = includeNumbersCheckBox.isChecked
        val includeSymbols = includeSymbolsCheckBox.isChecked

        // 检查是否至少选择了一种字符类型
        if (!includeLetters && !includeUppercase && !includeNumbers && !includeSymbols) {
            Toast.makeText(this, getString(R.string.select_at_least_one), Toast.LENGTH_SHORT).show()
            return
        }

        val password = createPassword(length, includeLetters, includeUppercase, includeNumbers, includeSymbols)
        if (password.isNotEmpty()) {
            val coloredPassword = createColoredPassword(password)
            passwordTextView.text = coloredPassword
            copyButton.visibility = Button.VISIBLE
        }
    }

    private fun createPassword(
        length: Int,
        includeLetters: Boolean,
        includeUppercase: Boolean,
        includeNumbers: Boolean,
        includeSymbols: Boolean
    ): String {
        val lowercaseLetters = "abcdefghijklmnopqrstuvwxyz"
        val uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val numbers = "0123456789"
        val symbols = "!@-$()"

        var characterPool = ""
        val requiredChars = mutableListOf<Char>()

        if (includeLetters) {
            characterPool += lowercaseLetters
            requiredChars.add(lowercaseLetters.random())
        }
        if (includeUppercase) {
            characterPool += uppercaseLetters
            requiredChars.add(uppercaseLetters.random())
        }
        if (includeNumbers) {
            characterPool += numbers
            requiredChars.add(numbers.random())
        }
        if (includeSymbols) {
            characterPool += symbols
            // 特殊字符个数在1-2之间
            val symbolCount = Random.nextInt(1, 2) // 1或2个特殊字符
            repeat(symbolCount) {
                requiredChars.add(symbols.random())
            }
        }

        if (characterPool.isEmpty()) return ""

        // 如果必需字符数量已经超过或等于密码长度，直接返回打乱的必需字符
        if (requiredChars.size >= length) {
            return requiredChars.take(length).shuffled().joinToString("")
        }

        // 确保每种选中的字符类型至少出现一次
        val password = StringBuilder()
        requiredChars.forEach { password.append(it) }

        // 填充剩余长度
        repeat(length - requiredChars.size) {
            password.append(characterPool[Random.nextInt(characterPool.length)])
        }

        // 打乱顺序
        return password.toString().toList().shuffled().joinToString("")
    }

    private fun createColoredPassword(password: String): SpannableString {
        val spannableString = SpannableString(password)

        for (i in password.indices) {
            val char = password[i]
            val color = when {
                char.isDigit() -> Color.RED
                char.isLetter() -> Color.BLACK
                "!@-_\$()=[]".contains(char) -> Color.BLUE
                else -> Color.BLACK
            }
            spannableString.setSpan(
                ForegroundColorSpan(color),
                i, i + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return spannableString
    }

    private fun copyPasswordToClipboard(password: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(getString(R.string.generated_password), password)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, getString(R.string.password_copied), Toast.LENGTH_SHORT).show()
    }

    private fun copyToClipboard() {
        val password = passwordTextView.text.toString()
        if (password.isNotEmpty() && password != getString(R.string.password_placeholder)) {
            copyPasswordToClipboard(password)

            // 添加到历史记录
            if (!passwordHistory.contains(password)) {
                passwordHistory.add(0, password)
                if (passwordHistory.size > MAX_HISTORY_SIZE) {
                    // 最多保存20条记录
                    passwordHistory.removeAt(passwordHistory.size - 1)
                }
                historyAdapter.notifyDataSetChanged()
                savePasswordHistory() // 保存到SharedPreferences
            }
        }
    }
}

class PasswordHistoryAdapter(
    private val passwords: List<String>,
    private val onCopyClick: (String) -> Unit
) : RecyclerView.Adapter<PasswordHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val passwordText: TextView = view.findViewById(R.id.historyPasswordText)
        val copyButton: ImageButton = view.findViewById(R.id.historyCopyButton)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_password_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val password = passwords[position]

        // 创建带颜色的密码显示
        val spannableString = SpannableString(password)
        for (i in password.indices) {
            val char = password[i]
            val color = when {
                char.isDigit() -> Color.RED
                char.isLetter() -> Color.BLACK
                "!@-_\$()=[]".contains(char) -> Color.BLUE
                else -> Color.BLACK
            }
            spannableString.setSpan(
                ForegroundColorSpan(color),
                i, i + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        holder.passwordText.text = spannableString
        holder.copyButton.setOnClickListener {
            onCopyClick(password)
        }
    }

    override fun getItemCount() = passwords.size
}