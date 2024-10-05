package library.transform

interface ITransformer {
    val transformName: String
    fun shouldTransform(className: String): Boolean
    fun transform(classBytes: ByteArray): ByteArray
}