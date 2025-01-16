package dictionary

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.util.concurrent.Executors
import javax.swing.*

object Display {

    private val dispatcher = Executors.newCachedThreadPool().asCoroutineDispatcher()
    private val job = SupervisorJob()
    private val scope = CoroutineScope(dispatcher + job)
    private val repository = Repository

    private val enterWordLabel = JLabel("Enter word: ")
    private val searchField = JTextField(20)

    private val resultArea = JTextArea(25, 50).apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
    }

    private val searchButton = JButton("Search").apply {
        addActionListener {
            scope.launch {
                isEnabled = false
                resultArea.text = "Loading..."
                val word = searchField.getText().trim()
                val definition = repository.loadDefinition(word).joinToString("\n\n")
                resultArea.text = definition.ifEmpty { "Not found" }
                isEnabled = true
            }
        }
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

    fun show() {
        mainFrame.isVisible = true
    }
}