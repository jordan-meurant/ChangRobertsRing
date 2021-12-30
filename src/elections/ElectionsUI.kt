package elections

import kotlinx.coroutines.Job
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.*
import javax.swing.*
import javax.swing.table.DefaultTableModel


private val INSETS = Insets(3, 10, 3, 10)
private val COLUMNS = arrayOf("Result")

@Suppress("CONFLICTING_INHERITED_JVM_DECLARATIONS")
class ElectionsUI : JFrame("Chang Roberts ring algorithm"), Elections {
    private val numberOfNode = JTextField(20)
    private val variant = JComboBox<Variant>(Variant.values())
    private val start = JButton("Start Election")
    private val cancel = JButton("Cancel").apply { isEnabled = false }

    private val resultsModel = DefaultTableModel(COLUMNS, 0)
    private val results = JTable(resultsModel)
    private val resultsScroll = JScrollPane(results).apply {
        preferredSize = Dimension(1000, 1000)
    }

    private val loadingIcon = ImageIcon(javaClass.classLoader.getResource("ajax-loader.gif"))
    private val loadingStatus = JLabel("Start new loading", loadingIcon, SwingConstants.CENTER)

    override val job = Job()

    init {
        // Create UI
        rootPane.contentPane = JPanel(GridBagLayout()).apply {
            addLabeled("Nombre de noeuds à créer", numberOfNode)
            addLabeled("Variant", variant)
            addWideSeparator()
            addWide(JPanel().apply {
                add(start)
                add(cancel)
            })
            addWide(resultsScroll) {
                weightx = 1.0
                weighty = 1.0
                fill = GridBagConstraints.BOTH
            }
            addWide(loadingStatus)
        }

        numberOfNode.addKeyListener(object : KeyAdapter() {
            override fun keyTyped(e: KeyEvent) {
                val c = e.keyChar
                if (!(c in '0'..'9' ||
                            c.code == KeyEvent.VK_BACK_SPACE ||
                            c.code == KeyEvent.VK_DELETE)
                ) {
                    toolkit.beep()
                    e.consume()
                }
            }
        })
        // Initialize actions
        init()
    }

    override fun getSelectedVariant(): Variant = variant.getItemAt(variant.selectedIndex)

    override fun updateResult(result: ArrayList<String>) {
        resultsModel.setDataVector(result.map { arrayOf(it) }.toTypedArray(), COLUMNS)
    }

    override fun setLoadingStatus(text: String, iconRunning: Boolean) {
        loadingStatus.text = text
        loadingStatus.icon = if (iconRunning) loadingIcon else null
    }

    override fun addCancelListener(listener: ActionListener) {
        cancel.addActionListener(listener)
    }

    override fun removeCancelListener(listener: ActionListener) {
        cancel.removeActionListener(listener)
    }

    override fun addLoadListener(listener: () -> Unit) {
        start.addActionListener { listener() }
    }

    override fun addOnWindowClosingListener(listener: () -> Unit) {
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                listener()
            }
        })
    }

    override fun setActionsStatus(newLoadingEnabled: Boolean, cancellationEnabled: Boolean) {
        start.isEnabled = newLoadingEnabled
        cancel.isEnabled = cancellationEnabled
    }

    override fun setParams(params: Params) {
        numberOfNode.text = params.getNumberOfNodes.toString()
        variant.selectedIndex = params.variant.ordinal
    }

    override fun getParams(): Params {

        return Params(numberOfNode.text.toInt(),getSelectedVariant())
    }
}

fun JPanel.addLabeled(label: String, component: JComponent) {
    add(JLabel(label), GridBagConstraints().apply {
        gridx = 0
        insets = INSETS
    })
    add(component, GridBagConstraints().apply {
        gridx = 1
        insets = INSETS
        anchor = GridBagConstraints.WEST
        fill = GridBagConstraints.HORIZONTAL
        weightx = 1.0
    })
}

fun JPanel.addWide(component: JComponent, constraints: GridBagConstraints.() -> Unit = {}) {
    add(component, GridBagConstraints().apply {
        gridx = 0
        gridwidth = 2
        insets = INSETS
        constraints()
    })
}

fun JPanel.addWideSeparator() {
    addWide(JSeparator()) {
        fill = GridBagConstraints.HORIZONTAL
    }
}

fun setDefaultFontSize(size: Float) {
    for (key in UIManager.getLookAndFeelDefaults().keys.toTypedArray()) {
        if (key.toString().lowercase().contains("font")) {
            val font = UIManager.getDefaults().getFont(key) ?: continue
            val newFont = font.deriveFont(size)
            UIManager.put(key, newFont)
        }
    }
}