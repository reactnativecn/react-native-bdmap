/**
 * Created by tdzl2003 on 4/25/16.
 */

import React, {
  requireNativeComponent,
  PropTypes,
  View,
  UIManager,
} from 'react-native'

require('react-native/Libraries/NativeApp/RCTNativeAppEventEmitter');

//const BDMapInfoWindow = require('./BDMapInfoWindow');
var resolveAssetSource = require('react-native/Libraries/Image/resolveAssetSource');

const defaultMarkerIcon = require('../assets/icon.png');

class BDMapMarker extends React.Component {
  static propTypes = {
    /** 继承自View的属性 **/
    ...View.propTypes,

    /** 选项 **/


    /** 属性 **/

    // 标注的位置
    latitude: PropTypes.number,
    longitude: PropTypes.number,

    // 标注图案
    icon: PropTypes.oneOfType([
      PropTypes.shape({
        uri: PropTypes.string,
      }),
      // Opaque type returned by require('./image.jpg')
      PropTypes.number,
    ]),

    title: PropTypes.string,

    // 信息窗口的内容和相对坐标
    infoView: PropTypes.element,
    infoYOffset: PropTypes.number,

    /** 事件 **/
    onPress: PropTypes.func,
  };
  static defaultProps = {
  };
  constructor(props) {
    super(props);
    this._location = {
      latitude: props.latitude,
      longitude: props.longitude,
    };
  }

  componentWillReceiveProps(nextProps){
    if (nextProps.latitude !== this._location.latitude || nextProps.longitude !== this._location.longitude){
      this._location = {
        latitude: nextProps.latitude,
        longitude: nextProps.longitude,
      };
    }
  }
  componentDidMount() {
    UIManager.dispatchViewManagerCommand(
      React.findNodeHandle(this),
      UIManager.RCTBDMapMarker.Commands.display,
      null
    );
  }
  //renderInfoWindow() {
  //  const {infoView} = this.props;
  //  if (!infoView) {
  //    return null;
  //  }
  //  return (
  //    <BDMapInfoWindow
  //      latitude={this.props.latitude}
  //      longitude={this.props.longitude}
  //      yOffset={this.props.infoYOffset}>
  //      {infoView}
  //    </BDMapInfoWindow>
  //  )
  //}
  onPress = () => {
    const {onPress, mapView} = this.props;
    onPress && onPress();
    mapView.onMarkerPress(this);
  };
  onBlur = () => {
    const {onBlur} = this.props;
    onBlur && onBlur();
  };
  onFocus = () => {
    const {onFocus} = this.props;
    onFocus && onFocus();
  };
  render() {
    var icon = resolveAssetSource(this.props.icon || defaultMarkerIcon);
    if (icon && icon.uri === '') {
      console.warn('source.uri should not be an empty string');
    }
    return <RCTBDMapMarker
        location={this._location}
        image={icon.uri}
        onPress={this.onPress}
        title={this.props.title}
      />;
  }
}

var RCTBDMapMarker = requireNativeComponent(`RCTBDMapMarker`, BDMapMarker, {
  nativeOnly: {
    location: true,
    image: true,
  }
});

module.exports = BDMapMarker;
