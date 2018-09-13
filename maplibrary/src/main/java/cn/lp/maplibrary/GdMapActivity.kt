package cn.lp.maplibrary

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import cn.shequren.map.R
import cn.shequren.map.R.layout
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps2d.AMap
import com.amap.api.maps2d.CameraUpdateFactory
import com.amap.api.maps2d.LocationSource
import com.amap.api.maps2d.model.*
import kotlinx.android.synthetic.main.activity_gd_map.*
import java.util.*

/**
 * 高德地图显示、定位、轨迹绘制、Marker点绘制
 */

class GdMapActivity : AppCompatActivity(), LocationSource, AMapLocationListener {


    /**
     * 地图控制器底下
     */
    lateinit var aMap: AMap


    var mListener: LocationSource.OnLocationChangedListener? = null
    var mlocationClient: AMapLocationClient? = null

    /**
     * 折线覆盖物对象
     */
    var polyline: Polyline? = null
    lateinit var mLocationOption: AMapLocationClientOption

    /**
     * 经纬度
     */
    var mLatitude: Double = 0.0
    var mLongitude: Double = 0.0


    /**
     * 成功回调
     */
    override fun onLocationChanged(aMapLocation: AMapLocation?) {
        //定位成功
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {

                mListener!!.onLocationChanged(aMapLocation)// 显示系统小蓝点
                val latLng = LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude())
                mLatitude = aMapLocation.getLatitude()
                mLongitude = aMapLocation.getLongitude()

                setLocationSuccess(latLng)

            } else {
                val errText = "定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo()
                Log.e("AmapErr", errText)
            }
        }
    }

    /**
     *停用回调
     */
    override fun deactivate() {
        mListener = null
        if (mlocationClient != null) {
            /**
             * 停止定位服务
             */
            mlocationClient!!.stopLocation()
            mlocationClient!!.onDestroy()
        }
        mlocationClient = null
    }

    /**
     * 激活定位
     */
    override fun activate(p0: LocationSource.OnLocationChangedListener?) {
        //设置定位参数和
        mListener = p0!!
        if (null == mlocationClient) {
            //定位服务类
            mlocationClient = AMapLocationClient(this)
            //定位的参数设置对象
            mLocationOption = AMapLocationClientOption()
            //设置定位监听
            mlocationClient!!.setLocationListener(this)
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy)

            //定位间隔时间
            mLocationOption!!.setInterval(1000 * 2 * 60)
            //设置定位参数
            mlocationClient!!.setLocationOption(mLocationOption)
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除

            //启用定位
            mlocationClient!!.startLocation()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_gd_map)
        // 此方法必须重写
        mapView.onCreate(savedInstanceState)


        intMap()
        intiView()
    }

    private fun intiView() {

        //修改地图模式
        type.setOnClickListener {
            if (aMap.mapType == AMap.MAP_TYPE_SATELLITE) {
                aMap.mapType = AMap.MAP_TYPE_NORMAL
            } else {
                aMap.mapType = AMap.MAP_TYPE_SATELLITE
            }

        }

        /**
         * 显示附近Marker覆盖物，像共享单车车站的位置
         */
        around.setOnClickListener {

            if (this.mLatitude != 0.0 && this.mLongitude != 0.0) {
                setAroundInfor(LatLng(mLatitude, mLongitude));
            } else {
                Toast.makeText(this, "位定位成功", Toast.LENGTH_LONG).show()
            }

        }

        /**
         * 显示轨迹，运动轨迹
         */
        trajectory.setOnClickListener {

            if (this.mLatitude != 0.0 && this.mLongitude != 0.0) {
                trajectory(LatLng(mLatitude, mLongitude));
            } else {
                Toast.makeText(this, "位定位成功", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun intMap() {
        this.aMap = mapView.map

        this.aMap.isTrafficEnabled = true// 显示实时交通状况
        //地图模式可选类型：MAP_TYPE_NORMAL,MAP_TYPE_SATELLITE,MAP_TYPE_NIGHT
        this.aMap.mapType = AMap.MAP_TYPE_NORMAL// 卫星地图模式

        ///aMap.setOnMapClickListener(this)  //点击地图监听
        aMap.setLocationSource(this)// 设置定位监听
        aMap.moveCamera(CameraUpdateFactory.zoomTo(16f))//设置缩放级别
        aMap.uiSettings.isMyLocationButtonEnabled = true// 设置默认定位按钮是否显示

        aMap.isMyLocationEnabled = true// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种

        //定位地图自动可以定位
        /* aMap.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {

             override fun onCameraChange(cameraPosition: CameraPosition) {}

             override fun onCameraChangeFinish(cameraPosition: CameraPosition) {
                 mLatitude = cameraPosition.target.latitude
                 mLongitude = cameraPosition.target.longitude
                 val latLng = LatLng(mLatitude, mLongitude)
                 //setLocationSuccess(latLng)
             }
         })*/

        aMap.moveCamera(CameraUpdateFactory.zoomTo(24f))

        /**
         * 设置地图小蓝点
         */
        val myLocationStyle = MyLocationStyle()
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(Color.BLACK))// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.BLACK)// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180))// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle.strokeWidth(2.0f)// 设置圆形的边框粗细
        aMap.setMyLocationStyle(myLocationStyle) // 将自定义的 myLocationStyle 对象添加到地图上
    }

    override fun onResume() {
        super.onResume()
        this.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        this.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        this.mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState)
    }


    private fun setLocationSuccess(latLng: LatLng) {
        aMap.animateCamera(CameraUpdateFactory.changeLatLng(latLng))

    }

    var isRningAroundInfor = false;

    /**
     * 显示周边
     */
    private fun setAroundInfor(latLng: LatLng) {
        /**
         * 使用固定变量判断，正式开发中根据业务需求决定是否重新绘制，需要注意的是重新绘制前需要清除绘制的信息，
         * 但是清除绘制会将从地图上删除所有的overlay（marker，circle，polyline 等对象）清除。
         */
        if (isRningAroundInfor) {
            Toast.makeText(this, "已经绘制过附近做坐标", Toast.LENGTH_LONG).show()
            //已经绘制过了
            return
        }
        isRningAroundInfor = true;
        /**
         * 真实开发中应该是一个Latlng的集合数据，这里就不用真实数据模拟20个点位数据
         */
        var lat: LatLng? = null
        var markerOptions = MarkerOptions()
        for (i in 1..20) {
            var a = Random().nextDouble() * 0.001;

            if (i / 6 == 0) {
                lat = LatLng(latLng.latitude + a, latLng.longitude + 5 * a)
            } else if (i / 6 == 1) {
                lat = LatLng(latLng.latitude + 3 * a, latLng.longitude - a)
            } else if (i / 6 == 2) {
                lat = LatLng(latLng.latitude - 2 * a, latLng.longitude + 5 * a)
            } else if (i / 6 == 3) {
                lat = LatLng(latLng.latitude - 5 * a, latLng.longitude - a)
            } else if (i / 6 == 3) {
                lat = LatLng(latLng.latitude - 4 * a, latLng.longitude + 3 * a)
            } else if (i / 6 == 4) {
                lat = LatLng(latLng.latitude + 5 * a, latLng.longitude - 2 * a)
            } else if (i / 6 == 5) {
                lat = LatLng(latLng.latitude - a, latLng.longitude - 3 * a)
            }

            markerOptions.position(lat)
            /**
             * 添加Marker覆盖物
             */
            markerOptions.icon(BitmapDescriptorFactory
                    .fromResource(R.drawable.express_icon_location_centre_point))
            markerOptions.snippet("描述")
            markerOptions.title("标题")
            aMap.addMarker(markerOptions);

        }

    }

    var isRningtrajectory = false;
    private fun trajectory(latLng: LatLng) {
        /**
         * 真实开发中应该是一个Latlng的集合数据，这里就不用真实数据模拟20个点位数据
         */


        isRningtrajectory = true

        var lats = arrayListOf<LatLng>();
        var lat: LatLng? = null

        for (i in 1..20) {
            var a = Random().nextDouble() * 0.001;
            if (i / 6 == 0) {
                lat = LatLng(latLng.latitude + a, latLng.longitude + 5 * a)
            } else if (i / 6 == 1) {
                lat = LatLng(latLng.latitude + 3 * a, latLng.longitude - a)
            } else if (i / 6 == 2) {
                lat = LatLng(latLng.latitude - 2 * a, latLng.longitude + 5 * a)
            } else if (i / 6 == 3) {
                lat = LatLng(latLng.latitude - 5 * a, latLng.longitude - a)
            } else if (i / 6 == 3) {
                lat = LatLng(latLng.latitude - 4 * a, latLng.longitude + 3 * a)
            } else if (i / 6 == 4) {
                lat = LatLng(latLng.latitude + 5 * a, latLng.longitude - 2 * a)
            } else if (i / 6 == 5) {
                lat = LatLng(latLng.latitude - a, latLng.longitude - 3 * a)
            }
            if (lat != null) {
                lats.add(lat)
            }
        }

        if (polyline != null) {
            /**
             * 异常覆盖物
             */
            polyline!!.remove()
        }

        /**
         * 添加运功轨迹 并获取添加物对象
         */
        polyline = aMap.addPolyline(PolylineOptions()
                //手动数据测试
                //.add(new LatLng(26.57, 106.71),new LatLng(26.14,105.55),new LatLng(26.58, 104.82), new LatLng(30.67, 104.06))
                //集合数据
                .addAll(lats).width(10f).setDottedLine(false).geodesic(true)
                //颜色
                .color(Color.argb(255, 255, 20, 147)));

    }
}
