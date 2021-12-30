package elections

fun main() {
    setDefaultFontSize(18f)
    ElectionsUI().apply {
        pack()
        setLocationRelativeTo(null)
        isVisible = true
    }
}