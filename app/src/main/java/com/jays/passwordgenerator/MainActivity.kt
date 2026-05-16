package com.jays.passwordgenerator

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jays.passwordgenerator.ui.theme.PasswordGeneratorTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import com.jays.passwordgenerator.data.PasswordDatabase
import com.jays.passwordgenerator.data.SavedPasswordEntity
import com.jays.passwordgenerator.ui.theme.NeonGreen
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            PasswordGeneratorTheme (){
                PasswordGeneratorScreen()
            }
        }
    }
}

@Composable
fun PasswordGeneratorScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current

    var selectedTabIndex by remember {
        mutableIntStateOf(0)
    }

    val tabs = listOf("Generator", "Vault")

    val database = remember {
        PasswordDatabase.getDatabase(context)
    }
    val dao = database.savedPasswordDao()

    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current


    var generatedPassword by remember {
        mutableStateOf("Tap Generate")
    }

    var passwordLength by remember {
        mutableFloatStateOf(14f)
    }

    var includeUppercase by remember {
        mutableStateOf(true)
    }

    var includeLowercase by remember {
        mutableStateOf(true)
    }

    var includeNumbers by remember {
        mutableStateOf(true)
    }

    var includeSymbols by remember {
        mutableStateOf(true)
    }

    var passwordTitle by remember {
        mutableStateOf("")
    }

    var savedPasswords by remember {
        mutableStateOf<List<SavedPasswordEntity>>(emptyList())
    }

    var showSavedPasswords by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        savedPasswords = dao.getAllPasswords()
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        //New
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🔐",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "Password Generator",
                    style = MaterialTheme.typography.headlineSmall
                )
            }


        // End New
        TabRow(
            selectedTabIndex = selectedTabIndex
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
                    },
                    text = {
                        Text(title)
                    }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> {
                GeneratorTabContent(
                    generatedPassword = generatedPassword,
                    passwordLength = passwordLength,
                    onPasswordLengthChange = { passwordLength = it },
                    includeUppercase = includeUppercase,
                    onIncludeUppercaseChange = { includeUppercase = it },
                    includeLowercase = includeLowercase,
                    onIncludeLowercaseChange = { includeLowercase = it },
                    includeNumbers = includeNumbers,
                    onIncludeNumbersChange = { includeNumbers = it },
                    includeSymbols = includeSymbols,
                    onIncludeSymbolsChange = { includeSymbols = it },
                    passwordTitle = passwordTitle,
                    onPasswordTitleChange = { passwordTitle = it },
                    onGeneratePassword = {
                        generatedPassword = generatePassword(
                            passwordLength = passwordLength.toInt(),
                            includeUppercase = includeUppercase,
                            includeLowercase = includeLowercase,
                            includeNumbers = includeNumbers,
                            includeSymbols = includeSymbols
                        )
                    },
                    onCopyGeneratedPassword = {
                        if (
                            generatedPassword != "Tap Generate" &&
                            generatedPassword != "Select at least one option"
                        ) {
                            clipboardManager.setText(AnnotatedString(generatedPassword))

                            Toast.makeText(
                                context,
                                "Generated password copied",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onSavePassword = {
                        if (
                            passwordTitle.isNotBlank() &&
                            generatedPassword != "Tap Generate" &&
                            generatedPassword != "Select at least one option"
                        ) {
                            scope.launch {
                                dao.insertPassword(
                                    SavedPasswordEntity(
                                        title = passwordTitle,
                                        password = generatedPassword
                                    )
                                )

                                savedPasswords = dao.getAllPasswords()
                                passwordTitle = ""

                                Toast.makeText(
                                    context,
                                    "Password saved",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                )
            }

            1 -> {
                VaultTabContent(
                    savedPasswords = savedPasswords,
                    showSavedPasswords = showSavedPasswords,
                    onToggleShowSavedPasswords = {
                        showSavedPasswords = !showSavedPasswords
                    },
                    onCopyPassword = { item ->
                        clipboardManager.setText(AnnotatedString(item.password))

                        Toast.makeText(
                            context,
                            "${item.title} password copied",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onDeletePassword = { item ->
                        scope.launch {
                            dao.deletePassword(item)
                            savedPasswords = dao.getAllPasswords()

                            Toast.makeText(
                                context,
                                "Password deleted",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
        }
    }
}
        /// tabbed Generator

@Composable
fun GeneratorTabContent(
    generatedPassword: String,
    passwordLength: Float,
    onPasswordLengthChange: (Float) -> Unit,
    includeUppercase: Boolean,
    onIncludeUppercaseChange: (Boolean) -> Unit,
    includeLowercase: Boolean,
    onIncludeLowercaseChange: (Boolean) -> Unit,
    includeNumbers: Boolean,
    onIncludeNumbersChange: (Boolean) -> Unit,
    includeSymbols: Boolean,
    onIncludeSymbolsChange: (Boolean) -> Unit,
    passwordTitle: String,
    onPasswordTitleChange: (String) -> Unit,
    onGeneratePassword: () -> Unit,
    onCopyGeneratedPassword: () -> Unit,
    onSavePassword: () -> Unit
) {

    var showPasswordOptions by remember{
        mutableStateOf(false)
    }

    var showGeneratedPassword by remember{
        mutableStateOf(false)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Generated Password:",
            style = MaterialTheme.typography.bodyLarge
        )

        // show Tap Generate or hidden password
        if(generatedPassword !== "Tap Generate")
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),

                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = if (showGeneratedPassword)
                        generatedPassword
                    else
                        "********",


                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = NeonGreen
                    ),
                    fontFamily = FontFamily.Monospace,
                )

                IconButton(
                    onClick = {
                        showGeneratedPassword = !showGeneratedPassword
                    }
                ) {

                    Icon(
                        imageVector =
                            if (showGeneratedPassword)
                                Icons.Default.VisibilityOff
                            else
                                Icons.Default.Visibility,

                        contentDescription = "Toggle Password Visibility",

                        tint = NeonGreen
                    )
                }
            }
        }else
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),

                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = generatedPassword,

                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = NeonGreen
                        ),
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }

        Button(
            onClick = onCopyGeneratedPassword,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Copy Generated Password")
        }

        Text(
            text = "Password Length: ${passwordLength.toInt()}",
            style = MaterialTheme.typography.bodyLarge
        )

        Slider(
            value = passwordLength,
            onValueChange = onPasswordLengthChange,
            valueRange = 6f..32f,
            steps = 25
        )

        Button(
            onClick = {
                showPasswordOptions = !showPasswordOptions
            },
            modifier = Modifier.fillMaxWidth()
        ){
            Text(
                if (showPasswordOptions) "Hide Password Options ▲" else "Show Password Options ▼"
            )
        }

        if(showPasswordOptions) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column (
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PasswordOptionRow(
                        label = "Uppercase",
                        checked = includeUppercase,
                        onCheckedChange = onIncludeUppercaseChange
                    )

                    PasswordOptionRow(
                        label = "Lowercase",
                        checked = includeLowercase,
                        onCheckedChange = onIncludeLowercaseChange
                    )

                    PasswordOptionRow(
                        label = "Numbers",
                        checked = includeNumbers,
                        onCheckedChange = onIncludeNumbersChange
                    )

                    PasswordOptionRow(
                        label = "Symbols",
                        checked = includeSymbols,
                        onCheckedChange = onIncludeSymbolsChange
                    )
                }
            }
        }

        OutlinedTextField(
            value = passwordTitle,
            onValueChange = onPasswordTitleChange,
            label = {
                Text("Password Title")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onGeneratePassword,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate Password")
        }

        Button(
            onClick = onSavePassword,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Password")
        }
    }


@Composable
fun VaultTabContent(
    savedPasswords: List<SavedPasswordEntity>,
    showSavedPasswords: Boolean,
    onToggleShowSavedPasswords: () -> Unit,
    onCopyPassword: (SavedPasswordEntity) -> Unit,
    onDeletePassword: (SavedPasswordEntity) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Saved Passwords",
            style = MaterialTheme.typography.titleMedium
        )

        Button(
            onClick = onToggleShowSavedPasswords,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (showSavedPasswords) "Hide Saved Passwords" else "Show Saved Passwords"
            )
        }

        savedPasswords.forEach { item ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall
                    )

                    Text(
                        text = if (showSavedPasswords) item.password else "••••••••••••",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onCopyPassword(item)
                            }
                        ) {
                            Text("Copy")
                        }

                        Button(
                            onClick = {
                                onDeletePassword(item)
                            }
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordOptionRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )

        Text(text = label)
    }
}

fun generatePassword(passwordLength: Int,includeUppercase: Boolean,
                     includeLowercase: Boolean,
                     includeNumbers: Boolean,
                     includeSymbols: Boolean): String{
    var characters = ""

    if (includeUppercase){
        characters += "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    }

    if (includeLowercase){
        characters += "abcdefghijklmnopqrstuvwxyz"
    }

    if (includeNumbers){
        characters += "0123456789"
    }

    if (includeSymbols){
        characters += "!@#$%^&*"
    }

    if (characters.isEmpty()){
        return "Select at least one option"
    }


    return (1..passwordLength)
        .map {characters.random()}
        .joinToString ("")
}

