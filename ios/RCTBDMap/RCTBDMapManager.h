//
//  RCTBDMapManager.h
//  RCTBDMap
//
//  Created by DengYun on 4/27/16.
//  Copyright © 2016 DengYun. All rights reserved.
//

#import <React/RCTViewManager.h>

#import <BaiduMapAPI_Map/BMKMapComponent.h>//引入地图功能所有的头文件

@interface RCTBDMapManager : RCTViewManager<BMKMapViewDelegate>

@end
