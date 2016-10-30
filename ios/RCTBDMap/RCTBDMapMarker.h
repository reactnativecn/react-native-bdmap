//
//  RCBBDMapMarker.h
//  RCTBDMap
//
//  Created by DengYun on 4/28/16.
//  Copyright © 2016 DengYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RCTViewManager.h"
#import <BaiduMapAPI_Map/BMKMapComponent.h>//引入地图功能所有的头文件


@interface RCTBDMapMarker : UIView <BMKAnnotation>

@property (nonatomic, readonly) CLLocationCoordinate2D coordinate;
@property (nonatomic) CLLocationCoordinate2D location;

@property (nonatomic, strong) UIImage *annotationImage;

@property (nonatomic) BMKMapView* mapView;
@property (nonatomic) bool isDisplayed;
@property (nonatomic) bool isRemoved;

@end

@interface RCTBDMapMarkerManager : RCTViewManager
@end