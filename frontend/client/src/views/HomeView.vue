<template>
  <div class="home-container">
    <!-- 导航模式遮罩层 -->
    <div v-if="isNavigating && navRoute" class="nav-overlay">
      <!-- 退出导航按钮 (悬浮在左侧，搜索框下方) -->
      <div class="nav-exit-btn" @click="endNavigation">
        <van-icon name="cross" />
        <span>退出导航</span>
      </div>
      
      <!-- 紧凑型底部导航面板 -->
      <div class="nav-bottom-compact">
        <!-- 主要指令区 -->
        <div class="nav-main-instr">
          <div class="instr-icon-box" :style="{ backgroundColor: getStepColor(navRoute.steps[0]) }">
            <van-icon :name="getStepIcon(navRoute.steps[0])" color="white" size="20" />
          </div>
          <div class="instr-content">
            <div class="instr-text">{{ navRoute.steps[0].instruction }}</div>
            <div class="instr-meta">
              <span>{{ navRoute.distance }}公里</span>
              <span class="separator">·</span>
              <span>{{ navRoute.time }}分钟</span>
            </div>
          </div>
        </div>

        <!-- 可折叠的详细路线列表 (只显示前2条，更多内容可滑动) -->
        <div class="nav-mini-list">
          <div 
            v-for="(step, index) in navRoute.steps.slice(1)" 
            :key="index" 
            class="mini-step-item"
          >
            <van-icon :name="getStepIcon(step)" :color="getStepColor(step)" size="14" style="margin-right: 6px;" />
            <span class="mini-step-text">{{ step.instruction }}</span>
            <span class="mini-step-dist">{{ step.distance }}米</span>
          </div>
          <div class="mini-step-item end-point">
            <van-icon name="location" color="#ee0a24" size="14" style="margin-right: 6px;" />
            <span class="mini-step-text">到达 {{ selectedPoi?.name }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 顶部搜索栏 -->
    <div class="search-box">
      <van-search
        v-model="searchText"
        placeholder="搜索地点/停车场"
        shape="round"
        background="transparent"
        @search="onSearch"
        @update:model-value="onSearchInput"
        @focus="onSearchFocus"
        @clear="clearSearch"
      />
      <!-- 搜索建议列表 -->
      <div v-if="showSuggestions && searchSuggestions.length > 0" class="suggestion-list">
        <div 
          v-for="(item, index) in searchSuggestions" 
          :key="index" 
          class="suggestion-item"
          @click="selectSuggestion(item)"
        >
          <van-icon name="location-o" />
          <div class="suggestion-info">
            <div class="suggestion-name">{{ item.name }}</div>
            <div class="suggestion-district">{{ item.district }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 功能按钮组 -->
    <div class="func-group">
      <div class="func-btn" @click="findNearby('停车场')">
        <div class="func-icon-wrapper">
          <img :src="parkingIconImg" class="func-icon-img" style="width: 24px; height: 24px; object-fit: contain;" />
        </div>
        <span>停车场</span>
      </div>
      <div class="func-btn" @click="findNearby('加油站')">
        <div class="func-icon-wrapper">
          <img :src="gasIconImg" class="func-icon-img" style="width: 24px; height: 24px; object-fit: contain;" />
        </div>
        <span>加油站</span>
      </div>
      <div class="func-btn" @click="findNearby('充电桩')">
        <div class="func-icon-wrapper">
          <img :src="chargingIconImg" class="func-icon-img" style="width: 24px; height: 24px; object-fit: contain;" />
        </div>
        <span>充电桩</span>
      </div>
      <div class="func-btn" @click="toggleManualMode">
        <van-icon :name="isManualMode ? 'success' : 'aim'" :color="isManualMode ? '#07c160' : '#409eff'" />
        <span>{{ isManualMode ? '确认' : '定位' }}</span>
      </div>
    </div>

    <!-- 地图中心红钉（手动定位用） -->
    <div v-show="isManualMode" class="center-pin">
      <van-icon name="location" color="#ee0a24" size="32" />
      <div class="pin-tip">拖动地图选择位置</div>
    </div>

    <!-- 当前位置提示条 -->
    <div v-if="currentAddress" class="current-location-bar">
      <van-icon name="location" color="#1989fa" />
      <span class="address-text">{{ currentAddress }}</span>
    </div>

    <!-- 地图容器 -->
    <div id="map-container"></div>

    <!-- POI 详情卡片 -->
    <van-action-sheet
      v-model:show="showPoiCard"
      :title="''"
      :closeable="false"
    >
      <div v-if="selectedPoi" class="poi-card-modern" style="padding: 20px; position: relative;">
        <!-- 关闭按钮 -->
        <div class="close-btn" @click="showPoiCard = false" style="position: absolute; top: 20px; right: 20px; z-index: 10;">
          <van-icon name="cross" size="22" color="#999" />
        </div>

        <div class="poi-header-modern">
          <div class="poi-info-left">
            <!-- 第一行：位置名称 + 运营中标识 -->
            <div class="poi-name-row" style="display: flex; align-items: center; padding-right: 30px; margin-bottom: 8px;">
              <span class="poi-name" style="font-size: 18px; font-weight: bold; color: #333; margin-right: 8px;">{{ selectedPoi.name }}</span>
              <van-tag type="success" round size="medium">运营中</van-tag>
            </div>
            
            <!-- 第二行：地址 + 距离 -->
            <div class="poi-meta-row" style="display: flex; align-items: center; color: #666; font-size: 13px;">
              <span class="poi-address-text" style="margin-right: 12px;">{{ selectedPoi.address }}</span>
              <span class="poi-distance" v-if="selectedPoi.distance">
                <van-icon name="guide-o" style="vertical-align: -2px;" />
                距您 {{ selectedPoi.distance > 1000 ? (selectedPoi.distance / 1000).toFixed(1) + 'km' : selectedPoi.distance + 'm' }}
              </span>
            </div>
            
            <!-- 车位信息 (仅已注册停车场显示) -->
            <div v-if="selectedPoi.isRegistered" class="poi-spots-row" style="margin-top: 8px; font-size: 14px;">
              <span style="color: #1989fa; font-weight: bold;">剩余车位: {{ selectedPoi.availableSpots }}</span>
              <span style="color: #999; margin-left: 8px;">(总车位: {{ selectedPoi.totalSpots }})</span>
            </div>
          </div>
        </div>

        <div class="poi-actions-modern" style="margin-top: 24px; position: relative; z-index: 100;">
          <div class="action-item" @click.stop="handleBook">
            <div class="action-icon-box book-icon">
              <van-icon name="clock-o" />
            </div>
            <span class="action-label">立即预约</span>
          </div>
          <div class="action-item main-action" @click.stop="handleNavigation">
            <div class="action-icon-box nav-icon">
              <van-icon name="guide-o" />
            </div>
            <span class="action-label">开始导航</span>
          </div>
        </div>
      </div>
    </van-action-sheet>

    <!-- 天气悬浮窗 -->
    <div 
      ref="weatherBtn" 
      class="weather-float-btn"
      :style="weatherBtnStyle"
      @click="showWeatherMenuFunc"
      @touchstart="onTouchStart"
      @touchmove="onTouchMove"
      @touchend="onTouchEnd"
    >
      <van-icon name="cloudy" size="24" color="#fff" />
      <span v-if="weatherInfo" class="weather-temp">{{ weatherInfo.temperature }}°</span>
    </div>

    <!-- 天气/AI 面板 -->
    <van-action-sheet v-model:show="showWeatherMenu" title="智停助手">
      <div class="weather-panel">
        <div v-if="weatherInfo" class="weather-card">
          <div class="weather-header">
            <div class="temp-section">
              <span class="temp-val">{{ weatherInfo.temperature }}</span>
              <span class="temp-unit">°C</span>
            </div>
            <div class="weather-desc-section">
              <div class="weather-text">{{ weatherInfo.weather }}</div>
              <div class="weather-location">
                <van-icon name="location-o" />
                {{ weatherInfo.city }}
              </div>
            </div>
          </div>
          <div class="weather-grid">
            <div class="grid-item">
              <van-icon name="wap-nav" />
              <span>风向 {{ weatherInfo.winddirection }}</span>
            </div>
            <div class="grid-item">
              <van-icon name="underway-o" />
              <span>风力 {{ weatherInfo.windpower }}级</span>
            </div>
            <div class="grid-item">
              <van-icon name="water-o" />
              <span>湿度 {{ weatherInfo.humidity }}%</span>
            </div>
          </div>
        </div>

        <div class="ai-suggestion" v-if="aiSuggestion">
          <div class="title">
            <van-icon name="smile-o" />
            <span>出行建议</span>
          </div>
          <div class="content">
            {{ aiSuggestion }}
          </div>
        </div>
        <div v-else-if="aiLoading" class="ai-suggestion" style="text-align: center;">
          <van-loading size="24px">正在生成建议...</van-loading>
        </div>
      </div>
    </van-action-sheet>

    <!-- 预约确认弹窗 -->
    <van-dialog
      v-model:show="showBookingConfirm"
      title="确认预约"
      show-cancel-button
      :loading="isProcessing"
      @confirm="confirmBooking"
    >
      <div style="padding: 20px; text-align: center;">
        <p>您即将预约：{{ selectedPoi?.name }}</p>
        <p style="color: #999; font-size: 12px;">预约后请尽快前往，系统将为您保留车位。</p>
      </div>
    </van-dialog>

    <!-- 支付弹窗 -->
    <van-popup
      v-model:show="showPayment"
      position="bottom"
      round
      :style="{ height: '50%' }"
    >
      <div style="padding: 20px;">
        <h3 style="text-align: center; margin-bottom: 20px;">选择支付方式</h3>
        <van-radio-group v-model="paymentMethod">
          <van-cell-group>
            <van-cell title="支付宝" clickable @click="paymentMethod = 'ALIPAY'">
              <template #right-icon>
                <van-radio name="ALIPAY" />
              </template>
              <template #icon>
                <van-icon name="alipay" color="#1677ff" size="24" style="margin-right: 10px;" />
              </template>
            </van-cell>
            <van-cell title="微信支付" clickable @click="paymentMethod = 'QR'">
              <template #right-icon>
                <van-radio name="QR" />
              </template>
              <template #icon>
                <van-icon name="qr" color="#07c160" size="24" style="margin-right: 10px;" />
              </template>
            </van-cell>
            <van-cell title="银行卡" clickable @click="paymentMethod = 'CARD'">
              <template #right-icon>
                <van-radio name="CARD" />
              </template>
              <template #icon>
                <van-icon name="card" color="#ff976a" size="24" style="margin-right: 10px;" />
              </template>
            </van-cell>
          </van-cell-group>
        </van-radio-group>
        
        <div style="margin-top: 30px;">
          <van-button type="primary" block round :loading="isProcessing" @click="handlePayment">
            立即支付
          </van-button>
        </div>
      </div>
    </van-popup>

    <!-- 自定义定位按钮 (替代原生定位控件) -->
    <div class="custom-geo-btn" @click="backToOrigin">
      <van-icon name="aim" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from "vue";
import { useRouter } from "vue-router";
import { showToast } from "vant";
import AMapLoader from "@amap/amap-jsapi-loader";
import { 
  getParkingLots, 
  amapIpLocate, 
  amapWeather, 
  amapPlaceText,
  chatAssistant,
  amapInputTips,
  amapPlaceAround,
  amapRegeocode,
  book,
  payRecord,
  getParkingLotByName
} from "../api/client";

import parkingIconImg from "@/assets/images/tingchechang.png";
import gasIconImg from "@/assets/images/jiayouzhan.png";
import chargingIconImg from "@/assets/images/chongdianzhuang.png";

const router = useRouter();

// 地图相关
const map = ref<any>(null);
const AMapObj = ref<any>(null); // 保存 AMap 对象
const searchText = ref("");
const parkingLots = ref<any[]>([]);
const markers = ref<any[]>([]);
const searchSuggestions = ref<any[]>([]);
const showSuggestions = ref(false);
const currentAddress = ref("");
const driving = ref<any>(null);
const nearbyMarkers = ref<any[]>([]);
const showPoiCard = ref(false);
const selectedPoi = ref<any>(null);
const searchMarker = ref<any>(null);
const isCheckingRegistration = ref(false);

// 预约与支付相关
const showBookingConfirm = ref(false);
const showPayment = ref(false);
const currentRecord = ref<any>(null);
const paymentMethod = ref("ALIPAY");
const isProcessing = ref(false);

// 手动定位相关
const isManualMode = ref(false);
const blueDotMarker = ref<any>(null);
const currentLocation = ref<number[]>([]); // [lng, lat]

// 天气/AI 相关
const showWeatherMenu = ref(false);
const weatherInfo = ref<any>(null);
const aiSuggestion = ref("");
const aiLoading = ref(false);
let isClickOnMarker = false;

// 导航相关
const isNavigating = ref(false);
const navRoute = ref<any>(null);
const isTextNavMode = ref(false);

const navThemeClass = computed(() => {
  const type = selectedPoi.value?.type;
  if (type === '加油站') return 'theme-gas';
  if (type === '充电桩') return 'theme-charging';
  return 'theme-parking';
});

const navThemeColor = computed(() => {
  const type = selectedPoi.value?.type;
  if (type === '加油站') return '#f56c6c';
  if (type === '充电桩') return '#e6a23c';
  return '#1989fa';
});

// 悬浮窗拖拽
const isDragging = ref(false);
const weatherBtnStyle = ref({ right: '20px', bottom: '100px' });
let startX = 0;
let startY = 0;
let startRight = 0;
let startBottom = 0;

const onTouchStart = (e: TouchEvent) => {
  isDragging.value = false;
  startX = e.touches[0].clientX;
  startY = e.touches[0].clientY;
  const style = window.getComputedStyle((e.target as HTMLElement).closest('.weather-float-btn') as HTMLElement);
  startRight = parseInt(style.right);
  startBottom = parseInt(style.bottom);
};

const onTouchMove = (e: TouchEvent) => {
  isDragging.value = true;
  e.preventDefault();
  const deltaX = startX - e.touches[0].clientX; // 向左拖动，right 增加
  const deltaY = startY - e.touches[0].clientY; // 向上拖动，bottom 增加
  weatherBtnStyle.value = {
    right: `${startRight + deltaX}px`,
    bottom: `${startBottom + deltaY}px`
  };
};

const onTouchEnd = () => {
  setTimeout(() => {
    isDragging.value = false;
  }, 100);
};

// 回到原点（哈尔滨信息工程学院）
const backToOrigin = () => {
  const origin = [127.2123395022321, 45.74563148731417];
  currentLocation.value = origin;
  
  if (map.value) {
    map.value.setZoomAndCenter(15, origin);
  }
  
  // 更新蓝点位置
  if (blueDotMarker.value) {
    blueDotMarker.value.setPosition(origin);
  }
  
  showToast("已回到当前位置");
};

// 初始化地图
const initMap = async () => {
  try {
    const AMap = await AMapLoader.load({
      key: "***********", // 高德地图web端(js api)密钥
      version: "2.0",
      plugins: ["AMap.Geolocation", "AMap.PlaceSearch", "AMap.Driving", "AMap.AutoComplete"]
    });
    AMapObj.value = AMap;

    // 默认位置（哈尔滨信息工程学院）
    const defaultPosition = [127.2123395022321, 45.74563148731417];
    currentLocation.value = defaultPosition;

    map.value = new AMap.Map("map-container", {
      zoom: 15,
      center: defaultPosition,
      resizeEnable: true
    });

    // 创建蓝点标记（当前位置）
    const blueDotContent = `
      <div class="blue-dot-marker">
        <div class="dot-inner"></div>
        <div class="dot-ring"></div>
      </div>
    `;
    
    blueDotMarker.value = new AMap.Marker({
      position: defaultPosition,
      content: blueDotContent,
      offset: new AMap.Pixel(-12, -12),
      zIndex: 100
    });
    map.value.add(blueDotMarker.value);

    // 监听地图点击，关闭卡片
    map.value.on('click', () => {
       // 地图点击时不做操作，因为 ActionSheet 自带遮罩层点击关闭
    });

    // 初始化驾车插件
    driving.value = new AMap.Driving({
      map: map.value,
      // panel: "panel" // 自定义 UI，不使用默认面板
      hideMarkers: true // 导航时隐藏起点终点默认标记，使用我们自己的或者默认的都行，这里保留默认
    });

    // 定位
    const geolocation = new AMap.Geolocation({
      enableHighAccuracy: true,
      timeout: 10000,
      zoomToAccuracy: true,
      buttonPosition: 'RB'
    });
    
    // map.value.addControl(geolocation); // 移除原生定位控件，使用自定义按钮
    
    // 优先使用浏览器定位
    geolocation.getCurrentPosition(async (status: string, result: any) => {
      if (status === 'complete' && result.position) {
        // 定位成功
        const pos = [result.position.lng, result.position.lat];
        updateCurrentLocation(pos);
        map.value.setCenter(pos);
      } else {
        // 定位失败，使用默认位置（哈尔滨信息工程学院）
        console.warn("Geolocation failed, using default location...");
        updateCurrentLocation(defaultPosition);
        if (map.value) {
          map.value.setCenter(defaultPosition);
        }
      }
    });

    // 加载停车场
    loadParkingLots(AMap);

    // 获取天气
    fetchWeather();

  } catch (e) {
    console.error("Map init failed", e);
    showToast("地图加载失败，请检查网络");
  }
};

const loadParkingLots = async (AMap: any) => {
  try {
    const res = await getParkingLots();
    if (res) {
      parkingLots.value = res;
      // 清除旧标记
      map.value.remove(markers.value);
      markers.value = [];

      res.forEach((lot: any) => {
        // 自定义停车场标记
        const markerDiv = document.createElement('div');
        markerDiv.className = 'custom-poi-marker marker-parking';
        markerDiv.innerHTML = `
            <div class="marker-icon">
              <img src="${parkingIconImg}" style="width: 36px; height: 36px; object-fit: cover; border-radius: 50%; display: block;" />
            </div>
            <div class="marker-arrow"></div>
        `;

        // 直接绑定 DOM 点击事件
        markerDiv.addEventListener('click', (e) => {
          e.stopPropagation(); // 阻止冒泡
          selectedPoi.value = {
            id: lot.lotId,
            name: lot.name,
            location: [lot.longitude, lot.latitude],
            address: lot.address,
            availableSpots: lot.availableSpots,
            totalSpots: lot.totalSpots,
            type: 'parking', // UI usage
            lotType: lot.type, // 0=Public, 1=Commercial
            distance: calculateDistance(currentLocation.value, [lot.longitude, lot.latitude]),
            isRegistered: true
          };
          showPoiCard.value = true;
        });

        // 添加触摸事件支持（以防 click 在某些移动设备上不灵敏）
        markerDiv.addEventListener('touchend', (e) => {
          e.stopPropagation();
          e.preventDefault(); // 阻止生成 click 事件，防止穿透到遮罩层导致卡片关闭
          
          selectedPoi.value = {
            id: lot.lotId,
            name: lot.name,
            location: [lot.longitude, lot.latitude],
            address: lot.address,
            availableSpots: lot.availableSpots,
            totalSpots: lot.totalSpots,
            type: 'parking',
            lotType: lot.type,
            distance: calculateDistance(currentLocation.value, [lot.longitude, lot.latitude]),
            isRegistered: true
          };
          showPoiCard.value = true;
        });

        const marker = new AMap.Marker({
          position: [lot.longitude, lot.latitude],
          title: lot.name,
          content: markerDiv,
          offset: new AMap.Pixel(-20, -45), // 调整偏移量以对齐底部尖角
          zIndex: 50,
          clickable: true,
          cursor: 'pointer'
        });
        
        marker.setMap(map.value);
        markers.value.push(marker);
      });
    }
  } catch (e) {
    console.error("Failed to load parking lots", e);
  }
};

const fetchWeather = async () => {
  try {
    const ipRes = await amapIpLocate();
    if (ipRes && ipRes.adcode) {
      const weatherRes = await amapWeather(ipRes.adcode);
      if (weatherRes.lives && weatherRes.lives.length > 0) {
        weatherInfo.value = weatherRes.lives[0];
      }
    }
  } catch (e) {
    console.error("Weather fetch failed", e);
  }
};

const fetchWeatherAndAi = async () => {
  if (!weatherInfo.value) {
    await fetchWeather();
  }
  
  if (weatherInfo.value && !aiSuggestion.value) {
    aiLoading.value = true;
    try {
      const prompt = `当前${weatherInfo.value.city}天气：${weatherInfo.value.weather}，温度${weatherInfo.value.temperature}度。请给出一个简短的出行或停车建议。`;
      const res = await chatAssistant(prompt);
      aiSuggestion.value = res.answer;
    } catch (e) {
      aiSuggestion.value = "暂时无法获取建议。";
    } finally {
      aiLoading.value = false;
    }
  }
};

const showWeatherMenuFunc = () => {
  if (isDragging.value) return;
  showWeatherMenu.value = true;
  fetchWeatherAndAi();
};

const onSearch = async () => {
  if (!searchText.value) return;
  // 隐藏建议列表
  showSuggestions.value = false;
  
  try {
    const res = await amapPlaceText(searchText.value);
    if (res.pois && res.pois.length > 0) {
      const poi = res.pois[0];
      if (poi.location) {
        let lng: number, lat: number;
        if (typeof poi.location === 'string') {
          const [lngStr, latStr] = poi.location.split(',');
          lng = parseFloat(lngStr);
          lat = parseFloat(latStr);
        } else {
          // 假设是对象
          lng = poi.location.lng;
          lat = poi.location.lat;
        }

        if (!isNaN(lng) && !isNaN(lat)) {
          map.value.setZoomAndCenter(15, [lng, lat]);
          showToast(`已定位到：${poi.name}`);
          currentAddress.value = poi.name;

          // 清除旧的搜索标记
          if (searchMarker.value) {
            map.value.remove(searchMarker.value);
          }

          // 添加新标记
          searchMarker.value = new AMapObj.value.Marker({
            position: [lng, lat],
            title: poi.name,
            icon: new AMapObj.value.Icon({
              size: new AMapObj.value.Size(25, 34),
              image: '//a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-default.png',
              imageSize: new AMapObj.value.Size(25, 34)
            })
          });
          map.value.add(searchMarker.value);

          // 显示详情卡片
          checkAndSetSelectedPoi({
            name: poi.name,
            location: [lng, lat],
            address: poi.address || poi.type
          });
        }
      }
    } else {
      showToast("未找到相关地点");
    }
  } catch (e) {
    console.error(e);
    showToast("搜索失败");
  }
};

const onSearchInput = async (val: string) => {
  if (!val) {
    showSuggestions.value = false;
    return;
  }
  try {
    const res = await amapInputTips(val);
    if (res.tips) {
      searchSuggestions.value = res.tips;
      showSuggestions.value = true;
    }
  } catch (e) {
    console.error(e);
  }
};

const selectSuggestion = (item: any) => {
  searchText.value = item.name;
  showSuggestions.value = false;
  
  if (item.location && typeof item.location === 'string' && item.location.length > 0) {
    const [lngStr, latStr] = item.location.split(',');
    const lng = parseFloat(lngStr);
    const lat = parseFloat(latStr);
    
    if (!isNaN(lng) && !isNaN(lat)) {
      map.value.setZoomAndCenter(15, [lng, lat]);
      currentAddress.value = item.name;
      
      // 清除旧的搜索标记
      if (searchMarker.value) {
        map.value.remove(searchMarker.value);
      }

      // 添加新标记
      searchMarker.value = new AMapObj.value.Marker({
        position: [lng, lat],
        title: item.name,
        icon: new AMapObj.value.Icon({
          size: new AMapObj.value.Size(25, 34),
          image: '//a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-default.png',
          imageSize: new AMapObj.value.Size(25, 34)
        })
      });
      map.value.add(searchMarker.value);

      // 显示详情卡片
      checkAndSetSelectedPoi({
        name: item.name,
        location: [lng, lat],
        address: item.district || item.address
      });
    } else {
      // 坐标解析失败，尝试直接搜索
      onSearch();
    }
  } else {
    // 无坐标，尝试直接搜索
    onSearch();
  }
};

const clearSearch = () => {
  showSuggestions.value = false;
  searchSuggestions.value = [];
};

const onSearchFocus = () => {
  if (searchText.value) {
    onSearchInput(searchText.value);
  }
};

// 更新当前位置（蓝点位置）
const updateCurrentLocation = (pos: number[]) => {
  currentLocation.value = pos;
  if (blueDotMarker.value) {
    blueDotMarker.value.setPosition(pos);
  }
  // 更新地址文字
  updateAddressText(pos);
};

// 更新地址文字
const updateAddressText = async (pos: number[]) => {
  const location = `${pos[0]},${pos[1]}`;
  try {
    const res = await amapRegeocode(location);
    if (res.regeocode && res.regeocode.formatted_address) {
      currentAddress.value = res.regeocode.formatted_address;
    }
  } catch (e) {
    // ignore
  }
};

// 查找周边（加油站/充电桩/停车场）
const findNearby = async (type: string) => {
  if (!map.value) return;
  showToast(`正在查找附近的${type}...`);
  
  // 使用当前位置作为中心搜索
  const center = currentLocation.value;
  const location = `${center[0]},${center[1]}`;
  
  try {
    const res = await amapPlaceAround(location, type);
    if (res.pois && res.pois.length > 0) {
      // 清除之前的周边标记
      if (nearbyMarkers.value.length > 0) {
        map.value.remove(nearbyMarkers.value);
        nearbyMarkers.value = [];
      }

      const AMap = AMapObj.value;

      res.pois.forEach((poi: any) => {
        const [lng, lat] = poi.location.split(',');
        
        // 根据类型设置不同的图标和颜色
        let imgSrc = parkingIconImg;
        let markerClass = '';
        
        if (type === '加油站') {
          imgSrc = gasIconImg;
          markerClass = 'marker-gas';
        } else if (type === '充电桩') {
          imgSrc = chargingIconImg;
          markerClass = 'marker-charging';
        } else if (type === '停车场') {
          imgSrc = parkingIconImg;
          markerClass = 'marker-parking';
        }

        const markerDiv = document.createElement('div');
        markerDiv.className = `custom-poi-marker ${markerClass}`;
        markerDiv.innerHTML = `
            <div class="marker-icon">
              <img src="${imgSrc}" style="width: 100%; height: 100%; object-fit: cover; border-radius: 50%; display: block;" />
            </div>
            <div class="marker-arrow" style="border-top-color: white;"></div>
        `;

        const handleMarkerClick = () => {
          checkAndSetSelectedPoi({
            name: poi.name,
            location: [lng, lat],
            address: poi.address,
            type: type,
            distance: calculateDistance(currentLocation.value, [lng, lat])
          });
        };

        // DOM 事件监听
        markerDiv.addEventListener('click', (e) => {
          e.stopPropagation();
          handleMarkerClick();
        });

        markerDiv.addEventListener('touchend', (e) => {
          e.stopPropagation();
          e.preventDefault(); // 阻止生成 click 事件
          handleMarkerClick();
        });

        const marker = new AMap.Marker({
          position: [lng, lat],
          title: poi.name,
          content: markerDiv,
          offset: new AMap.Pixel(-20, -45),
          zIndex: 50,
          clickable: true,
          cursor: 'pointer'
        });

        marker.setMap(map.value);
        nearbyMarkers.value.push(marker);
      });

      // 使用 setFitView 自动调整视野，避免锁死
      map.value.setFitView(nearbyMarkers.value, false, [60, 60, 60, 60]); // padding
      showToast(`找到${res.pois.length}个${type}`);
    } else {
      showToast(`附近没有找到${type}`);
    }
  } catch (e) {
    showToast("查找失败");
    console.error(e);
  }
};

const toggleTextNav = () => {
  isTextNavMode.value = !isTextNavMode.value;
};

const openExternalMap = () => {
  showToast("开始导航");
};

// 开始导航
const startNavigation = (destination: number[]) => {
  if (!driving.value || !map.value) return;
  
  // 使用当前定位作为起点
  const start = currentLocation.value;
  if (start && start.length === 2) {
    driving.value.search(start, destination, (status: string, result: any) => {
      if (status === 'complete') {
        showToast("路线规划成功");
        if (result.routes && result.routes.length > 0) {
          const route = result.routes[0];
          navRoute.value = {
            time: Math.ceil(route.time / 60),
            distance: (route.distance / 1000).toFixed(1),
            steps: route.steps
          };
          isNavigating.value = true;
          isTextNavMode.value = false;
        }
      } else {
        showToast("路线规划失败");
      }
    });
  } else {
    showToast("无法获取当前位置，无法导航");
  }
};

const endNavigation = () => {
  isNavigating.value = false;
  navRoute.value = null;
  isTextNavMode.value = false;
  if (driving.value) {
    driving.value.clear();
  }
};

// 切换手动定位模式
const toggleManualMode = () => {
  if (!map.value) return;

  if (!isManualMode.value) {
    // 开启手动模式
    isManualMode.value = true;
    // 移动地图中心到当前蓝点位置
    map.value.setCenter(currentLocation.value);
    showToast("拖动地图选择位置");
  } else {
    // 确认修改位置
    const center = map.value.getCenter();
    const newPos = [center.getLng(), center.getLat()];
    
    updateCurrentLocation(newPos);
    isManualMode.value = false;
    showToast("位置已修改");

    // 刷新周边停车场（可选：如果需要重新加载平台停车场）
    // loadParkingLots(AMapObj.value); // 如果停车场是基于位置加载的，可以开启
  }
};

// 地图移动结束
const onMapMoveEnd = async () => {
  if (!map.value) return;
  // 只有在手动模式下，可能需要显示地图中心点的临时地址（如果需要的话）
  // 但根据需求：拖动地图不会改变顶部的“当前位置”文字，文字始终锁定在确认后的位置。
  // 所以这里不需要做什么，除非我们想在红钉下方显示一个小地址提示。
  // 这里暂时保持空白，严格遵循需求。
};

const checkAndSetSelectedPoi = async (poiData: any) => {
  // 先显示基本信息
  selectedPoi.value = {
    ...poiData,
    isRegistered: false // 默认为未注册
  };
  
  isCheckingRegistration.value = true;
  showPoiCard.value = true;
  
  try {
    const realLot = await getParkingLotByName(poiData.name);
    if (realLot) {
       selectedPoi.value = {
         ...poiData,
         id: realLot.lotId,
         availableSpots: realLot.availableSpots,
         totalSpots: realLot.totalSpots,
         lotType: realLot.type, // 0 or 1
         isRegistered: true
       };
    }
  } catch (e) {
     // Not found or error, keep as unregistered
     console.log("Lot validation failed", e);
  } finally {
    isCheckingRegistration.value = false;
  }
};

const handleBook = () => {
  console.log("handleBook clicked", selectedPoi.value);
  if (isCheckingRegistration.value) {
    showToast("正在查询停车场信息，请稍候");
    return;
  }
  if (selectedPoi.value && selectedPoi.value.isRegistered) {
    showBookingConfirm.value = true;
  } else {
    showToast("停车场并未注册");
  }
};

const confirmBooking = async () => {
  if (!selectedPoi.value || !selectedPoi.value.id) return;
  
  isProcessing.value = true;
  try {
    const res = await book(selectedPoi.value.id);
    currentRecord.value = res;
    showBookingConfirm.value = false;
    showPoiCard.value = false;
    
    // Check lot type
    if (selectedPoi.value.lotType === 1) { // Commercial
      showPayment.value = true;
    } else {
      showToast("预约成功 (公共停车场免费)");
      // Refresh parking lots to update availability
      if (AMapObj.value) {
        loadParkingLots(AMapObj.value);
      }
    }
  } catch (e: any) {
    showToast("预约失败: " + (e.message || "未知错误"));
  } finally {
    isProcessing.value = false;
  }
};

const handlePayment = async () => {
  if (!currentRecord.value) return;
  
  isProcessing.value = true;
  try {
    // Simulate payment delay
    await new Promise(resolve => setTimeout(resolve, 1500));
    
    await payRecord({
      recordId: currentRecord.value.recordId,
      method: paymentMethod.value
    });
    
    showPayment.value = false;
    showToast("支付成功，车位已保留");
    
    // Refresh parking lots to update availability
    if (AMapObj.value) {
      loadParkingLots(AMapObj.value);
    }
  } catch (e) {
    showToast("支付失败");
  } finally {
    isProcessing.value = false;
  }
};

const handleNavigation = () => {
  if (!selectedPoi.value || !selectedPoi.value.location) return;
  showPoiCard.value = false;
  startNavigation(selectedPoi.value.location);
};

// 计算距离
const calculateDistance = (start: number[], end: number[]) => {
  if (!AMapObj.value || !start || start.length < 2 || !end || end.length < 2) return 0;
  const p1 = new AMapObj.value.LngLat(start[0], start[1]);
  const p2 = new AMapObj.value.LngLat(end[0], end[1]);
  return Math.round(p1.distance(p2));
};

// ----------------------
// 新增：嵌入式文字导航逻辑
// ----------------------
const currentStepIndex = ref(0);
const stepListRef = ref<HTMLElement | null>(null);
const isSimulating = ref(false);
let simulationTimer: any = null;

// 获取路段颜色
const getStepColor = (step: any) => {
  const instr = step.instruction || '';
  const action = step.action || '';
  
  if (instr.includes('左转') || action.includes('左')) return '#1989fa'; // 蓝色
  if (instr.includes('右转') || action.includes('右')) return '#e6a23c'; // 橙色
  if (instr.includes('掉头') || action.includes('掉头')) return '#ee0a24'; // 红色
  if (instr.includes('直行') || action.includes('直行')) return '#07c160'; // 绿色
  return '#909399'; // 灰色
};

// 获取路段图标
const getStepIcon = (step: any) => {
  const instr = step.instruction || '';
  const action = step.action || '';
  
  if (instr.includes('左转') || action.includes('左')) return 'arrow-left';
  if (instr.includes('右转') || action.includes('右')) return 'arrow'; // Vant icon name for right arrow is usually arrow, but let's check. actually 'arrow' is right chevron. 'share' is curved arrow? Let's use simple arrows.
  // Vant icons: arrow-left, arrow (right), arrow-up, arrow-down.
  // Actually, let's use:
  // Left: arrow-left
  // Right: arrow
  // Straight: arrow-up
  // U-turn: replay
  
  if (instr.includes('左转') || action.includes('左')) return 'arrow-left';
  if (instr.includes('右转') || action.includes('右')) return 'arrow';
  if (instr.includes('掉头') || action.includes('掉头')) return 'replay';
  if (instr.includes('直行') || action.includes('直行')) return 'arrow-up';
  
  return 'guide-o';
};

// 模拟导航
const simulateNavigation = () => {
  if (isSimulating.value) {
    // 停止模拟
    isSimulating.value = false;
    if (simulationTimer) clearInterval(simulationTimer);
    return;
  }

  if (!navRoute.value || !navRoute.value.steps) return;
  
  isSimulating.value = true;
  currentStepIndex.value = 0;
  
  // 每2秒走一步
  simulationTimer = setInterval(() => {
    if (currentStepIndex.value < navRoute.value.steps.length - 1) {
      currentStepIndex.value++;
      scrollToCurrentStep();
    } else {
      // 到达终点
      isSimulating.value = false;
      clearInterval(simulationTimer);
      showToast("模拟导航结束");
    }
  }, 2000);
};

// 滚动到当前步骤
const scrollToCurrentStep = () => {
  const el = document.getElementById(`step-${currentStepIndex.value}`);
  if (el && stepListRef.value) {
    el.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }
};

onMounted(() => {
  initMap();
});

onUnmounted(() => {
  if (map.value) {
    map.value.destroy();
  }
  if (simulationTimer) clearInterval(simulationTimer);
});
</script>

<style>
/* 全局样式，用于自定义 Marker */
.custom-poi-marker {
  width: 40px;
  height: 50px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: transform 0.2s;
  pointer-events: auto !important; /* Ensure click events work */
  cursor: pointer;
}

.custom-poi-marker:active {
  transform: scale(0.9);
}

.marker-icon {
  width: 36px;
  height: 36px;
  background: white;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 2;
  border: 2px solid white;
  overflow: hidden;
}

.marker-arrow {
    width: 0;
    height: 0;
    border-left: 6px solid transparent;
    border-right: 6px solid transparent;
    border-top: 8px solid white;
    margin-top: -2px;
    filter: drop-shadow(0 2px 2px rgba(0,0,0,0.1));
  }
  
  .parking-icon-text {
    font-size: 20px;
    font-weight: bold;
    color: #1989fa;
    line-height: 1;
  }

  .marker-parking .marker-icon {
    border-color: #1989fa;
  }

.marker-gas .marker-icon {
  border-color: #f56c6c;
}

.marker-charging .marker-icon {
  border-color: #e6a23c;
}

/* 蓝点 */
.blue-dot-marker {
  width: 24px;
  height: 24px;
  position: relative;
}

.dot-inner {
  width: 14px;
  height: 14px;
  background-color: #1989fa;
  border: 2px solid white;
  border-radius: 50%;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 2;
  box-shadow: 0 0 4px rgba(0,0,0,0.2);
}

.dot-ring {
  width: 24px;
  height: 24px;
  background-color: rgba(25, 137, 250, 0.3);
  border-radius: 50%;
  position: absolute;
  top: 0;
  left: 0;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(0.8); opacity: 0.8; }
  100% { transform: scale(1.5); opacity: 0; }
}

/* 导航相关样式 */
.nav-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 200;
  pointer-events: none; /* 让点击穿透到地图 */
  display: flex;
  flex-direction: column;
  justify-content: flex-end; /* 内容靠下 */
}

.nav-exit-btn {
  pointer-events: auto;
  position: absolute;
  top: 70px; /* 与右侧功能按钮对齐 */
  left: 10px;
  background: white;
  padding: 8px 14px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  display: flex;
  align-items: center;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  z-index: 202;
}

.nav-exit-btn .van-icon {
  margin-right: 4px;
  font-size: 16px;
  font-weight: bold;
}

/* 紧凑型底部导航面板 */
.nav-bottom-compact {
  pointer-events: auto;
  background: white;
  margin: 10px 10px 20px 10px;
  padding: 12px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
  max-height: 35vh; /* 进一步限制高度 */
}

.nav-main-instr {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.instr-icon-box {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}

.instr-content {
  flex: 1;
}

.instr-text {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 2px;
  line-height: 1.2;
}

.instr-meta {
  font-size: 12px;
  color: #666;
}

.instr-meta .separator {
  margin: 0 6px;
  color: #ddd;
}

/* 迷你列表 */
.nav-mini-list {
  border-top: 1px solid #f5f5f5;
  padding-top: 8px;
  overflow-y: auto;
  max-height: 100px;
}

.mini-step-item {
  display: flex;
  align-items: center;
  padding: 6px 0;
  font-size: 13px;
  color: #666;
}

.mini-step-text {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-right: 10px;
}

.mini-step-dist {
  font-size: 12px;
  color: #999;
}


</style>

<style scoped>
.home-container {
  width: 100%;
  height: calc(100vh - 56px); /* 减去底部导航栏高度，防止出现滚动条 */
  position: relative;
  overflow: hidden; /* 确保内部元素不溢出 */
}

#map-container {
  width: 100%;
  height: 100%;
}

.search-box {
  position: absolute;
  top: 10px;
  left: 10px;
  right: 10px;
  z-index: 205; /* 确保在导航遮罩层和按钮之上 */
}

.suggestion-list {
  background: white;
  margin-top: 5px;
  border-radius: 8px;
  max-height: 300px;
  overflow-y: auto;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.suggestion-item {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
}

.suggestion-item:active {
  background-color: #f5f5f5;
}

.suggestion-info {
  margin-left: 10px;
  flex: 1;
}

.suggestion-name {
  font-size: 14px;
  color: #333;
}

.suggestion-district {
  font-size: 12px;
  color: #999;
}

.func-group {
  position: absolute;
  top: 70px;
  right: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  z-index: 99;
}

.func-btn {
  width: 40px;
  height: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: #666;
  cursor: pointer;
}

.func-btn .van-icon {
  font-size: 20px;
  margin-bottom: 2px;
}

.map-center-crosshair {
  display: none;
}

.center-pin {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -100%); /* 钉子尖端对准中心 */
  z-index: 90;
  pointer-events: none;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.pin-tip {
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  margin-top: 4px;
  white-space: nowrap;
}

.current-location-bar {
  position: absolute;
  bottom: 80px; /* 降低高度 */
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.95);
  padding: 8px 16px;
  border-radius: 20px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #333;
  z-index: 90;
  max-width: 80%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  backdrop-filter: blur(5px);
}

.weather-float-btn {
  position: fixed;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1989fa, #6ba2f2);
  box-shadow: 0 4px 12px rgba(25, 137, 250, 0.3);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  z-index: 99;
  cursor: pointer;
  transition: transform 0.1s;
}

.weather-float-btn:active {
  transform: scale(0.95);
}

.weather-temp {
  font-size: 10px;
  margin-top: -2px;
}

.weather-panel {
  padding: 20px;
  min-height: 200px;
}

/* 天气卡片样式 */
.weather-card {
  background: linear-gradient(135deg, #6ba2f2 0%, #1989fa 100%);
  border-radius: 16px;
  padding: 20px;
  color: white;
  box-shadow: 0 4px 12px rgba(25, 137, 250, 0.3);
  margin-bottom: 20px;
  position: relative;
  overflow: hidden;
}

.weather-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.temp-section {
  display: flex;
  align-items: flex-start;
}

.temp-val {
  font-size: 48px;
  font-weight: bold;
  line-height: 1;
}

.temp-unit {
  font-size: 20px;
  margin-top: 8px;
  opacity: 0.8;
}

.weather-desc-section {
  flex: 1;
  margin-left: 15px;
}

.weather-text {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 4px;
}

.weather-location {
  font-size: 14px;
  opacity: 0.8;
  display: flex;
  align-items: center;
  gap: 4px;
}

.weather-grid {
  display: flex;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  padding: 12px;
}

.grid-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}

.grid-item .van-icon {
  font-size: 20px;
  opacity: 0.9;
}

.ai-suggestion {
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  border-radius: 16px;
  padding: 16px;
  border: none;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
  position: relative;
  overflow: hidden;
}

.ai-suggestion::before {
  content: '';
  position: absolute;
  top: -10px;
  right: -10px;
  width: 60px;
  height: 60px;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 50%;
}

.ai-suggestion .title {
  font-weight: bold;
  color: #0050b3;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
}

.ai-suggestion .content {
  font-size: 14px;
  color: #003a8c;
  line-height: 1.6;
  background: rgba(255, 255, 255, 0.6);
  padding: 10px;
  border-radius: 8px;
}

.poi-card {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.poi-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.poi-name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.poi-address {
  font-size: 14px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 4px;
}

.poi-actions {
  display: flex;
  gap: 15px;
  margin-top: 10px;
}

.action-btn {
  flex: 1;
}

/* 自定义蓝点标记 */
/* 已移至全局样式 */

/* 自定义POI标记 */
/* 已移至全局样式 */

/* 现代 POI 卡片 */
.poi-popup {
  overflow: visible;
  pointer-events: none; /* 让 popup 容器本身不阻挡点击，只有内容阻挡 */
}

.poi-card-modern {
  background: white;
  padding: 24px 20px 30px;
  border-radius: 20px 20px 0 0;
  position: relative;
  box-shadow: 0 -4px 24px rgba(0,0,0,0.15); /* 增加阴影，提升悬浮感 */
  pointer-events: auto; /* 内容区域恢复点击 */
}

.drag-handle {
  width: 40px;
  height: 4px;
  background: #e0e0e0;
  border-radius: 2px;
  position: absolute;
  top: 10px;
  left: 50%;
  transform: translateX(-50%);
}

.poi-header-modern {
  margin-bottom: 24px;
}

.poi-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.poi-name {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.poi-meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #666;
}

.poi-distance {
  display: flex;
  align-items: center;
  gap: 2px;
  color: #1989fa;
  font-weight: 500;
}

.poi-address-text {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.poi-actions-modern {
  display: flex;
  gap: 16px;
}

.action-item {
  flex: 1;
  background: #f7f8fa;
  border-radius: 12px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.2s;
  pointer-events: auto; /* Ensure clickable */
  position: relative;
  z-index: 10;
}

.action-item:active {
  background: #f0f1f5;
  transform: scale(0.98);
}

.action-item.main-action {
  background: #1989fa;
  color: white;
  flex: 1.5;
}

.action-icon-box {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.book-icon {
  background: white;
  color: #333;
}

.nav-icon {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.action-label {
  font-size: 14px;
  font-weight: 500;
}

.main-action .action-label {
  color: white;
}

.func-icon-wrapper {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 2px;
}

.func-icon-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.custom-geo-btn {
  position: absolute;
  right: 20px;
  bottom: 30px;
  width: 40px;
  height: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 150;
  cursor: pointer;
  color: #666;
  font-size: 24px;
}

.custom-geo-btn:active {
  background: #f5f5f5;
  color: #1989fa;
}
</style>