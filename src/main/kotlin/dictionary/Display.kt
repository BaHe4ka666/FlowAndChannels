package dictionary

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.awt.BorderLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.util.concurrent.Executors
import javax.swing.*

@OptIn(FlowPreview::class)
object Display {

    private val queries = MutableSharedFlow<String>()
    val state = MutableStateFlow<ScreenState>(ScreenState.Initial)

    private val dispatcher = Executors.newCachedThreadPool().asCoroutineDispatcher()
    private val scope = CoroutineScope(dispatcher)
    private val repository = Repository

    private val enterWordLabel = JLabel("Enter word: ")
    private val searchField = JTextField(20).apply {
        addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent?) {
                loadDefinitions()
            }
        })
    }

    private val searchButton = JButton("Search").apply {
        addActionListener {
            loadDefinitions()
        }
    }

    private val resultArea = JTextArea(25, 50).apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
    }

    private val topPanel = JPanel().apply {
        add(enterWordLabel)
        add(searchField)
        add(searchButton)
    }


    private val mainFrame = JFrame("Dictionary app").apply {
        layout = BorderLayout()
        add(topPanel, BorderLayout.NORTH)
        add(JScrollPane(resultArea), BorderLayout.CENTER)
        pack()
    }

    private fun loadDefinitions() {
        scope.launch {
            queries.emit(searchField.text.trim())
        }
    }

    init {
        queries.onEach {
                state.emit(ScreenState.Loading)
            }.debounce(500)
            .map {
                if (it.isEmpty()) {
                    state.emit(ScreenState.Initial)
                } else {
                    val result = repository.loadDefinition(it)
                    if (result.isEmpty()) {
                        state.emit(ScreenState.NotFound)
                    } else {
                        state.emit(ScreenState.DefinitionsLoaded(result))
                    }
                }
            }
            .retry() {
                state.emit(ScreenState.Error)
                true
            }.launchIn(scope)

        state.onEach {
            when (it) {
                is ScreenState.DefinitionsLoaded -> {
                    resultArea.text = it.definition.joinToString("\n\n")
                    searchButton.isEnabled = true
                }

                ScreenState.Initial -> {
                    resultArea.text = ""
                    searchButton.isEnabled = false
                }

                ScreenState.Loading -> {
                    resultArea.text = "Loading..."
                    searchButton.isEnabled = false
                }

                ScreenState.NotFound -> {
                    resultArea.text = "Not found"
                    searchButton.isEnabled = true
                }

                ScreenState.Error -> {
                    resultArea.text = "Something went wrong"
                    searchButton.isEnabled = true
                }
            }
        }.launchIn(scope)
    }

    fun show() {
        mainFrame.isVisible = true
    }
}

fun main() {
    Display.show()
    CoroutineScope(Dispatchers.IO).launch() {
        delay(10_000)
        println("Second subscriber")
        Display.state.collect {
            println(it)
        }
    }
}

