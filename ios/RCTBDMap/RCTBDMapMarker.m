//
//  RCBBDMapMarker.m
//  RCTBDMap
//
//  Created by DengYun on 4/28/16.
//  Copyright © 2016 DengYun. All rights reserved.
//

#import "RCTBDMapMarker.h"
#import "RCTUIManager.h"
#import "RCTImageLoader.h"
#import "RCTImageUtils.h"

@implementation RCTBDMapMarker

@synthesize location = _location;
@synthesize annotationImage = _annotationImage;
@synthesize isDisplayed = _isDisplayed;
@synthesize isRemoved = _isRemoved;
@synthesize mapView = _mapView;

- (instancetype)init {
    _isDisplayed = false;
    _isRemoved = false;
    return self;
}

- (void) display {
    if (!_isDisplayed && !_isRemoved && _annotationImage) {
        _isDisplayed = true;
            
        if (_mapView) {
            [_mapView addAnnotation:self];
//            [_mapView mapForceRefresh];
        }
    }
}

- (CLLocationCoordinate2D)coordinate
{
    return _location;
}

- (void)setCoordinate:(CLLocationCoordinate2D)newCoordinate
{
    _location = newCoordinate;
}

- (void)setAnnotationImage:(UIImage *)image {
    _annotationImage = image;
}

- (NSString *)title {
    return @"";
}

@end

@implementation RCTBDMapMarkerManager

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE(RCTBDMapMarkerManager)

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

- (UIView *)view
{
    RCTBDMapMarker* ret = [[RCTBDMapMarker alloc] init];
    return ret;
}

RCT_EXPORT_METHOD(display:(nonnull NSNumber *)reactTag)
{
//    [self.bridge.uiManager addUIBlock:
//     ^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry){
//         RCTBDMapMarker *view = viewRegistry[reactTag];
//         [view display];
//     }];
}

RCT_EXPORT_VIEW_PROPERTY(location, CLLocationCoordinate2D);

RCT_CUSTOM_VIEW_PROPERTY(image, NSString*, RCTBDMapMarker)
{
    [self.bridge.imageLoader loadImageWithURLRequest:[RCTConvert NSString:json] callback:^(NSError *error, UIImage *image) {
        dispatch_async([self methodQueue], ^(void) {
            view.annotationImage = image;
            [view display];
        });
    }];
}

@end