package de.achalmgucker.compass2

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() , SensorEventListener {

    val TAG = "Compass2"
    private var mSensorManager: SensorManager? = null
    private var mSensorAccelerometer: Sensor? = null
    private var mSensorMagnetometer: Sensor? = null
    private var mGravityData: FloatArray? = null
    private var mGeomagneticData: FloatArray? = null
    private var mImageView: ImageView? = null
    private var mTextView: TextView? = null
    private var hasSensors = false
    private var mAngle: Float = 0.0F

    var text_low_acc:String? = null
    var text_med_acc:String? = null
    var text_hi_acc:String? = null
    var text_unrel:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get access to our compass rose
        mImageView = findViewById<ImageView>(R.id.imageView)
        mTextView = findViewById<TextView>(R.id.textView)

        text_low_acc = getString(R.string.comp_lowacc)
        text_med_acc = getString(R.string.comp_medacc)
        text_hi_acc = getString(R.string.comp_hiacc)
        text_unrel = getString(R.string.comp_unrel)

        // We need accelerometer and magnetic field detector
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mSensorAccelerometer = mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mSensorMagnetometer = mSensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (mSensorAccelerometer == null || mSensorMagnetometer == null) {
            // required sensor not available
            mTextView!!.text = getString(R.string.info_nocompass)
        }
        else
            hasSensors = true
    }

    override fun onResume() {
        super.onResume()
        if (hasSensors) {
            mSensorManager?.registerListener(this, mSensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
            mSensorManager?.registerListener(this, mSensorMagnetometer, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        mSensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val tGrav = mGravityData
            if (tGrav == null) {
                mGravityData = event.values.copyOf(3)
            }
            else {
                tGrav[0] = (2.0F * tGrav[0] + event.values[0]) / 3.0F
                tGrav[1] = (2.0F * tGrav[1] + event.values[1]) / 3.0F
                tGrav[2] = (2.0F * tGrav[2] + event.values[2]) / 3.0F
            }
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            val tMag = mGeomagneticData
            if (tMag == null) {
                mGeomagneticData = event.values.copyOf(3)
            }
            else {
                tMag[0] = (2.0F * tMag[0] + event.values[0]) / 3.0F
                tMag[1] = (2.0F * tMag[1] + event.values[1]) / 3.0F
                tMag[2] = (2.0F * tMag[2] + event.values[2]) / 3.0F
            }
        }

        if (mGravityData != null && mGeomagneticData != null) {
            val r = FloatArray(9)
            val i = FloatArray(9)
            val success = SensorManager.getRotationMatrix(r, i, mGravityData, mGeomagneticData)
            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)
                val compassAngle = orientation[0] * 180.0F / Math.PI.toFloat()
                mAngle = (3.0F * mAngle + compassAngle) / 4.0F
                mImageView!!.rotation = -mAngle
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        when (sensor!!.type) {
            Sensor.TYPE_MAGNETIC_FIELD -> when (accuracy) {
                SensorManager.SENSOR_STATUS_UNRELIABLE-> mTextView?.text = text_unrel
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> mTextView?.text = text_low_acc
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> mTextView?.text = text_med_acc
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> mTextView?.text = text_hi_acc
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val a = menu?.add("About")
        a?.setOnMenuItemClickListener { showAbout() }
        return true
    }

    private fun showAbout(): Boolean {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)
        return true
    }
}