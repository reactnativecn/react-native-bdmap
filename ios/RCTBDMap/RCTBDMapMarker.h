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


@interface RCTBDMapMarker : NSObject <BMKAnnotation>

@property (nonatomic) NSString* key;

@property (nonatomic, readonly) CLLocationCoordinate2D coordinate;
@property (nonatomic) CLLocationCoordinate2D location;

@property (nonatomic) BMKAnnotationView* annoView;

@property (nonatomic) UIImage * image;
@property (nonatomic) NSString* imageUrl;

@end
