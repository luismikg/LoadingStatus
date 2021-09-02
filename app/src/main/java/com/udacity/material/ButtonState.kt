package com.udacity.material


sealed class ButtonState {
    object Clicked : ButtonState()
    object Download : ButtonState()
    object Loading : ButtonState()
    object Completed : ButtonState()
    object Unknown : ButtonState()
}