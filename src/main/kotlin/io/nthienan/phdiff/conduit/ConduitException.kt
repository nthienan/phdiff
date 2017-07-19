package io.nthienan.phdiff.conduit

/**
 * Created on 19-Jul-17.
 * @author nthienan
 */
class ConduitException : Exception {
    val code: Int

    constructor(message: String) : super(message) {
        this.code = 0
    }

    constructor(message: String, code: Int) : super(message) {
        this.code = code
    }
}
