package io.tripmate.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import android.widget.Button
import com.bumptech.glide.request.target.Target
import io.peanutsdk.util.bindView
import io.peanutsdk.widget.FourThreeImageView
import io.tripmate.R
import io.tripmate.util.GlideApp

/**
 * OnBoarding screen
 */
class OnBoardActivity : AppCompatActivity() {
    private val container: ViewGroup by bindView(R.id.container)
    private val image: FourThreeImageView by bindView(R.id.onboard_tut_image)
    private val finish: Button by bindView(R.id.finish)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboard_activity)

        //Load tutorial image
        GlideApp.with(this)
                .asGif()
                .load(R.drawable.trip_mate_tut)
                .placeholder(R.color.white)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .into(image)

        finish.setOnClickListener({
            startActivity(Intent(this@OnBoardActivity, AuthActivity::class.java))
            finishAfterTransition()
        })

    }

}
