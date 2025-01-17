package dictionary

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.util.concurrent.Executors
import javax.swing.*

@OptIn(FlowPreview::class)
object Display {

    private val queries = Channel<String>()
    private val state = MutableSharedFlow<ScreenState>()

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
            queries.send(searchField.text.trim())
        }
    }

    init {
        queries.consumeAsFlow()
            .onEach {
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
            }.launchIn(scope)

        state.onStart {
            emit(ScreenState.Initial)
        }
            .onEach {
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
            }
        }.launchIn(scope)
    }
}

