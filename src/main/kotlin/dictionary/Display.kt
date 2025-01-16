package dictionary

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.util.concurrent.Executors
import javax.swing.*

@OptIn(FlowPreview::class)
object Display {

    private val queries = Channel<String>()


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
                searchButton.isEnabled = false
                resultArea.text = "Loading..."

            }.debounce(500) /* Нам прилетел emit, мы установили состояние загрузки, после этого вызываем
            debounce. У нас произойдет ожидание 500 миллисекунд и если за это время новых emit'ов не поступило, то
            элемент дальше полетит по цепочке, если же за это время поступит еще один элемент, то старый отменяется и
            снова будет установлено ожидание в 500 миллисекунд.*/
            .map {
                repository.loadDefinition(it)
            }.map {
                it.joinToString("\n\n").ifEmpty { "Not found" }
            }.onEach {
                println(it)
                resultArea.text = it
                searchButton.isEnabled = true
            }.launchIn(scope)
    }


    fun show() {
        mainFrame.isVisible = true
    }
}