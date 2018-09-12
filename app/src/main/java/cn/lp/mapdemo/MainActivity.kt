package cn.lp.mapdemo

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cn.lp.maplibrary.Bd2MapActivity
import cn.lp.maplibrary.BdMapActivity
import cn.lp.maplibrary.GdMap2Activity
import cn.lp.maplibrary.GdMapActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_gd.setOnClickListener {
            startActivity(Intent(this, GdMapActivity::class.java))
        }
        btn_gd_map2.setOnClickListener {
            startActivity(Intent(this, GdMap2Activity::class.java))
        }

        btn_bd.setOnClickListener {
            startActivity(Intent(this, BdMapActivity::class.java))
        }
        btn_bd2.setOnClickListener {
            startActivity(Intent(this, Bd2MapActivity::class.java))
        }


    }
}
