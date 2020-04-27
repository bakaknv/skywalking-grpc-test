package nkg.example.service.extentions

import io.grpc.stub.StreamObserver

/**
 * @author nkalugin on 4/20/20.
 */

suspend fun <V> StreamObserver<V>.sendResponse(
    valueSupplier: suspend () -> V,
    onError: (exception: Exception) -> Unit
) {
    try {
        onNext(valueSupplier())
        onCompleted()
    } catch (onNextError: Exception) {
        try {
            onError(onNextError)
            this.onError(onNextError)
        } catch (fail: Exception) {
            onError(fail)
        }
    }
}