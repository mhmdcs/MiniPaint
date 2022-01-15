package com.example.minipaint

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import androidx.core.view.WindowCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Create an instance of MyCanvasView.
        val myCanvasView = MyCanvasView(this)

        // request the full screen for the layout of myCanvasView. Do this by setting the SYSTEM_UI_FLAG_FULLSCREEN flag on myCanvasView. In this way, the view completely fills the screen.
        myCanvasView.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN

        // Add a content description for accessibility.
        myCanvasView.contentDescription = getString(R.string.canvasContentDescription)

        //set the content view to myCanvasView
        setContentView(myCanvasView)


        //You will need to know the size of the view for drawing, but you cannot get the size of the view in the onCreate() method,
        // because the size has not been determined at this point, we will set that up in onSizeChanged in the custom view myCanvasView class
    }
}