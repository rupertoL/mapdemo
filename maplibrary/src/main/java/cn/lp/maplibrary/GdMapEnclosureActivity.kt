package cn.lp.maplibrary

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import cn.shequren.map.R.layout
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps2d.AMap
import com.amap.api.maps2d.CameraUpdateFactory
import com.amap.api.maps2d.LocationSource
import com.amap.api.maps2d.model.*
import kotlinx.android.synthetic.main.activity_gd_map_enclosure.*

/**
 * 高德地图使用api绘制电子围栏
 * 使用到的定位和其他的知识这里不在重复详细注释
 */
class GdMapEnclosureActivity : AppCompatActivity(), LocationSource, AMapLocationListener {


    lateinit var aMap: AMap
    var mListener: LocationSource.OnLocationChangedListener? = null
    var mlocationClient: AMapLocationClient? = null
    lateinit var mLocationOption: AMapLocationClientOption

    var mLatitude: Double = 0.0
    var mLongitude: Double = 0.0

    var iseDitMode = false;
    var latLngs = arrayListOf<LatLng>();
    var mPolygonOptions = arrayListOf<PolygonOptions>();
    var mPolygons = arrayListOf<Polygon>();
    var markerOptions = MarkerOptions()
    var marker: Marker? = null
    var polylines = arrayListOf<Polyline>();

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

    override fun deactivate() {
        //定位停止
        mListener = null
        if (mlocationClient != null) {
            mlocationClient!!.stopLocation()
            mlocationClient!!.onDestroy()
        }
        mlocationClient = null
    }

    override fun activate(p0: LocationSource.OnLocationChangedListener?) {
        //定位
        mListener = p0!!
        if (null == mlocationClient) {
            mlocationClient = AMapLocationClient(this)
            mLocationOption = AMapLocationClientOption()
            //			mLocationOption.setOnceLocation(true);
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

            mlocationClient!!.startLocation()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_gd_map_enclosure)

        mapView.onCreate(savedInstanceState)// 此方法必须重写


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
        around.setOnClickListener {


            if (around.text.equals("保存")) {
                savPolygonOptions()
            }

            iseDitMode = !iseDitMode;
            if (iseDitMode) {
                latLngs.clear()
                polylines.clear()
                around.setText("保存")
            } else {
                around.setText("添加")
            }
            trajectory.setText("显示围栏")
            closAllPolygonOptions()

        }
        trajectory.setOnClickListener {

            if (trajectory.text.equals("显示围栏")) {
                trajectory.setText("关闭围栏")
                showPolygonOptions()
            } else {
                trajectory.setText("显示围栏")
                closAllPolygonOptions()
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
        // aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCAT);


        aMap.moveCamera(CameraUpdateFactory.zoomTo(24f))
        //设置地图小蓝点
        val myLocationStyle = MyLocationStyle()
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(Color.BLACK))// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0))// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(150, 215, 238, 255))// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle.strokeWidth(2.0f)// 设置圆形的边框粗细
        aMap.setMyLocationStyle(myLocationStyle) // 将自定义的 myLocationStyle 对象添加到地图上

        aMap.setOnMapClickListener {
            /**
             * 根据用户在地图上的点击，绘制多边形的定点
             */
            if (iseDitMode) {
                latLngs.add(it)
                if (latLngs.size > 1) {
                    //这里逻辑在运动轨迹文章中已经介绍
                  var  polyline =  aMap.addPolyline(PolylineOptions()
                            //手动数据测试
                            //.add(new LatLng(26.57, 106.71),new LatLng(26.14,105.55),new LatLng(26.58, 104.82), new LatLng(30.67, 104.06))
                            //集合数据
                            .addAll(latLngs).width(4f).setDottedLine(false).geodesic(true)
                            //颜色
                            .color(Color.argb(255, 255, 68, 0)))
                    polylines.add(polyline)
                    this.marker!!.remove()
                } else {
                    markerOptions.position(it)
                    marker = aMap.addMarker(markerOptions);
                    trajectory(it)
                }
            }
        }
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


    private fun trajectory(latLng: LatLng) {

        polylines.add(aMap.addPolyline(PolylineOptions()
                //手动数据测试
                //.add(new LatLng(26.57, 106.71),new LatLng(26.14,105.55),new LatLng(26.58, 104.82), new LatLng(30.67, 104.06))
                //集合数据
                .add(latLng).width(4f).setDottedLine(false).geodesic(true)
                //颜色
                .color(Color.argb(255, 255, 68, 0))))
        aMap.invalidate()
    }


    /**
     * 根据多边形的定点绘制多边形，该对变形可作为电子围栏
     */
    private fun savPolygonOptions() {
        if (latLngs.size > 2) {

            latLngs.add(latLngs.get(0))
            mPolygonOptions.add(PolygonOptions().addAll(latLngs))

            /**
             * 移除绘制过程的定点覆盖物
             */
            for (polyline in polylines) {
                polyline.remove()
            }
            trajectory.setText("显示围栏")
            aMap.invalidate()
        } else {
            Toast.makeText(this, "至少添加三个点才能建立围栏", Toast.LENGTH_LONG).show()
        }

    }

    /**
     * 根据用户存储的多边形对象显示电子围栏
     * 通过aMap.addPolygon(polygonOptions)可以得到多边形对象
     * 通过polygon.contains(LatLng)可以判断出该Latlng对象是否在电子围栏中
     *
     */
    private fun showPolygonOptions() {

        for (polygonOptions in mPolygonOptions) {
            polygonOptions.fillColor(Color.argb(150, 239, 113, 113))
                    .strokeColor(Color.argb(150, 239, 113, 113)).strokeWidth(1f)
            var  polygon = aMap.addPolygon(polygonOptions);
            mPolygons.add(aMap.addPolygon(polygonOptions))
        }
    }

    /**
     * 关闭显示的电子围覆盖层
     */
    private fun closAllPolygonOptions() {

        for (polygon in mPolygons) {
            polygon.remove()
        }
        aMap.invalidate()
    }
}
