//
//  RCTBDMapManager.m
//  RCTBDMap
//
//  Created by DengYun on 4/27/16.
//  Copyright © 2016 DengYun. All rights reserved.
//

#import "RCTBDMapManager.h"
#import "RCTBDMap.h"
#import "RCTCovert+BDMap.h"
#import "RCTEventDispatcher.h"
#import "RCTBDMapMarker.h"
#import "MyAnimatedAnnotationView.h"
#import "UIView+React.h"

@interface RCTBMKMapView : BMKMapView

@property (nonatomic, copy) RCTDirectEventBlock onLoad;
@property (nonatomic, copy) RCTDirectEventBlock onRegionChange;
@property (nonatomic, copy) RCTDirectEventBlock onRegionChangeComplete;

@end

@implementation RCTBMKMapView{
    NSMutableArray<RCTBDMapMarker*> *_markers;
    BMKPolyline* trace;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        _markers = [NSMutableArray new];
    }
    return self;
}

- (NSArray<UIView *> *)reactSubviews
{
    return _markers;
}

- (void)insertReactSubview:(RCTBDMapMarker *)view atIndex:(NSInteger)atIndex
{
    if (![view isKindOfClass:[RCTBDMapMarker class]]) {
        RCTLogError(@"subview should be of type RCTBDMapMarker");
        return;
    }
    [_markers addObject:view];
    view.mapView = self;
    if (view.isDisplayed) {
        [self addAnnotation:view];
    }
}

- (void)removeReactSubview:(RCTBDMapMarker *)subview
{
    [_markers removeObject:subview];
    subview.isRemoved = true;
    if (subview.isDisplayed) {
        [self removeAnnotation:subview];
    }
}

- (void)setTraceData:(NSArray*) data
{
    if (self->trace != nil){
        [self removeOverlay: self->trace];
        self->trace = nil;
    }
    if (data != nil) {
        const int count = [data count];
        CLLocationCoordinate2D coords[count];
        for (int i = 0; i < count; i++) {
            coords[i].longitude = [data[i][0] doubleValue];
            coords[i].latitude = [data[i][1] doubleValue];
        }
        self->trace = [BMKPolyline polylineWithCoordinates:coords count:count];
        [self addOverlay:self->trace];
    }
}

@end

@implementation RCTBDMapManager

RCT_EXPORT_MODULE(RCTBDMapViewManager)

- (UIView *)view
{
    RCTBMKMapView* ret = [[RCTBMKMapView alloc] init];
    ret.delegate = self;
    return ret;
}

RCT_EXPORT_VIEW_PROPERTY(onLoad, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onRegionChange, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onRegionChangeComplete, RCTDirectEventBlock)

RCT_CUSTOM_VIEW_PROPERTY(region, BMKCoordinateRegion, RCTBMKMapView)
{
    if (json) {
        [view setRegion: [RCTConvert BMKCoordinateRegion:json] animated:YES];
    }
}

RCT_CUSTOM_VIEW_PROPERTY(traceData, NSArray*, RCTBMKMapView)
{
    if (json) {
        [view setTraceData: json];
    }
}

- (void)mapView:(RCTBMKMapView *)mapView regionDidChangeAnimated:(BOOL)animated{
    if (mapView.onRegionChange) {
        mapView.onRegionChange([RCTBDMap _convertRegion:mapView.region]);
    }
}

- (void)mapStatusDidChanged:(RCTBMKMapView *)mapView{
    if (mapView.onRegionChangeComplete) {
        mapView.onRegionChangeComplete([RCTBDMap _convertRegion:mapView.region]);
    }
}

- (void)mapViewDidFinishLoading:(RCTBMKMapView *)mapView
{
    if ((mapView).onLoad) {
        (mapView).onLoad(@{});
    }
}

// Override
- (BMKOverlayView *)mapView:(BMKMapView *)mapView viewForOverlay:(id <BMKOverlay>)overlay{
    if ([overlay isKindOfClass:[BMKPolyline class]]){
        BMKPolylineView* polylineView = [[BMKPolylineView alloc] initWithOverlay:overlay];
        polylineView.strokeColor = [[UIColor purpleColor] colorWithAlphaComponent:1];
        polylineView.lineWidth = 5.0;

        return polylineView;
    }
    return nil;
}

- (BMKAnnotationView *)mapView:(BMKMapView *)mapView viewForAnnotation:(id <BMKAnnotation>)annotation
{
    if ([annotation isKindOfClass:[RCTBDMapMarker class]]) {
        RCTBDMapMarker* marker = annotation;
        BMKAnnotationView* ret = [[BMKAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:nil];
        ret.image = marker.annotationImage;
        ret.canShowCallout = NO;
        return ret;
//        return [[RCTBDMapMarker alloc] initWithAnnotation:annotation reuseIdentifier:nil];
    }
    return nil;
}

- (void)mapView:(BMKMapView *)mapView didSelectAnnotationView:(BMKAnnotationView *)view{
    RCTBDMapMarker* marker = view.annotation;
//    if (marker.onPress) {
//        marker.onPress(@{});
//    }
    [self.bridge.eventDispatcher sendInputEventWithName:@"topPress" body:@{
                                                                            @"target": marker.reactTag
                                                                            }];
    view.selected = false;
}

@end
