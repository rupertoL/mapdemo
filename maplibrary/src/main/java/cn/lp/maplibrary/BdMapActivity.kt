package cn.lp.maplibrary

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cn.shequren.map.R
import cn.shequren.map.R.layout
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import kotlinx.android.synthetic.main.activity_bd_map.*
import java.util.*

/**
 * 百度地图显示、定位、轨迹绘制、Marker点绘制
 */
class BdMapActivity : AppCompatActivity() {


    lateinit var mBaiduMap: BaiduMap
    lateinit var mLocationClient: LocationClient
    var latLng: LatLng? = null
    var bdPolyline: Overlay? = null
    var bdMarker = arrayListOf<Overlay>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /**
         * 地图使用前必须要调用初始化，并且要在setContentView（）前
         */
        SDKInitializer.initialize(getApplicationContext());
        setContentView(layout.activity_bd_map)

        intMap()
        intiView()
    }

    private fun intiView() {

        //修改地图模式
        type.setOnClickListener {

            if (mBaiduMap.mapType == BaiduMap.MAP_TYPE_NORMAL) {
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
            } else {
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
            }

        }

        /**
         * 绘制锚点
         */
        around.setOnClickListener {
            setAroundInfor(this.latLng!!)
        }
        /**
         * 绘制轨迹
         */
        trajectory.setOnClickListener {

            trajectory(this.latLng!!);
        }
    }

    /**
     * 初始化地图对象
     */
    private fun intMap() {

        /**
         * 获取地图操作对象
         */
        mBaiduMap = bd_mapView.map;

        //设置地图的模式
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);

        //开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        //设置地图的缩放级别
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(16f));


        /**
         * 一下为定位的设置
         */
        mLocationClient = LocationClient(getApplicationContext());     //声明LocationClient类
        /**
         * 设置地图允许所有的手势操作，根据需要可以去设置那些打开那些关闭
         * 使用的类为：java.lang.Object
         *                  com.baidu.mapapi.map.UiSettings
         * 参考Api：http://wiki.lbsyun.baidu.com/cms/androidsdk/doc/v5.2.0/index.html
         */
        mBaiduMap.uiSettings.setAllGesturesEnabled(true)
        //注册监听函数
        var myLocationListener = MyLocationListener();
        initLocation()
        mLocationClient.registerLocationListener(myLocationListener);
        //开启定位
        mLocationClient.start();
        //图片点击事件，回到定位点
        mLocationClient.requestLocation();
    }

    //配置定位SDK参数
    private fun initLocation() {
        val option = LocationClientOption()
        option.locationMode = LocationClientOption.LocationMode.Hight_Accuracy//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll")//可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(1000)//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true)//可选，设置是否需要地址信息，默认不需要
        option.isOpenGps = true//可选，默认false,设置是否使用gps
        option.isLocationNotify = true//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true)//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation
        // .getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true)//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false)
        option.isOpenGps = true // 打开gps

        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false)//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false)//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.locOption = option
    }


    override fun onResume() {
        super.onResume()
        this.bd_mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        this.bd_mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        bd_mapView.onDestroy();
        MapView.setMapCustomEnable(false);
    }

    //实现BDLocationListener接口,BDLocationListener为结果监听接口，异步获取定位结果
    inner class MyLocationListener : BDAbstractLocationListener() {

        override fun onReceiveLocation(location: BDLocation) {

            latLng = LatLng(location.latitude, location.longitude)
            setLocationData(latLng!!)
        }

        override fun onConnectHotSpotMessage(s: String?, i: Int) {

        }
    }


    private fun setLocationData(latLng: LatLng) {
        // 设置定位数据
        mBaiduMap.setMyLocationData(MyLocationData.Builder()
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(100f).latitude(latLng.latitude)
                .longitude(latLng.longitude).build())

        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(latLng))

    }


    /**
     * 周边
     */
    private fun setAroundInfor(latLng: LatLng) {

        /**
         * 真实开发中应该是一个Latlng的集合数据，这里就不用真实数据模拟20个点位数据
         */
        var lat: LatLng? = null
        var markerOptions = arrayListOf<MarkerOptions>()


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

            var markerOption = MarkerOptions()
            markerOption.position(lat).icon(BitmapDescriptorFactory.fromResource(R.drawable.express_icon_location_centre_point))
            markerOption.title("标题")
            var bundle =  Bundle()
            bundle.putSerializable("info", "附加信息")
            markerOption.extraInfo(bundle)
            markerOptions.add(markerOption)


        }

        /**
         * 移除覆盖物
         */
        for (marker in bdMarker) {
            if (marker != null) {
                marker!!.remove()
            }
        }
        /**
         * 添加锚点覆盖物
         */
        bdMarker = mBaiduMap.addOverlays(markerOptions as List<OverlayOptions>?) as ArrayList<Overlay>
    }

    private fun trajectory(latLng: LatLng) {
        /**
         * 真实开发中应该是一个Latlng的集合数据，这里就不用真实数据模拟20个点位数据
         */


        var lats = arrayListOf<LatLng>()
        var mPolylineOptions = PolylineOptions()
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
        if (bdPolyline != null) {
            bdPolyline!!.remove()
        }
        if (mPolylineOptions.points != null && mPolylineOptions.points.size > 0) {
            mPolylineOptions.points.clear()
        }
        mPolylineOptions.points(lats).dottedLine(false).color(Color.argb(255, 255, 20, 147)).width(5)


        /**
         * 绘制运动轨迹
         */
        bdPolyline = mBaiduMap.addOverlay(mPolylineOptions);

    }

}
