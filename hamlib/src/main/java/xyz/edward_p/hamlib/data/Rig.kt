package xyz.edward_p.hamlib.data

data class Rig(
    val rigModel: Int,  /*!< Rig model. */
    val modelName: String,    /*!< Model name. */
    val mfgName: String,       /*!< Manufacturer. */
    val version: String,        /*!< Driver version. */
    val copyright: String,     /*!< Copyright info. */
    val portType: Int /*!< Type of communication port. */
)
