package io.github.myungpyo.simplekspsample.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.myungpyo.ei.R
import io.github.myungpyo.simplekspsample.StickyState

class MainActivity : AppCompatActivity() {

    private val stateBinding = MainActivityStateBinding()

    @StickyState
    var stringProp: String = "aaa"

    @StickyState
    var intProp: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        stateBinding.restore(stateHolder = this, stateStore = savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        stateBinding.save(stateHolder = this, stateStore = outState)
        super.onSaveInstanceState(outState)
    }
}