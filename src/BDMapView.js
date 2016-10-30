/**
 * Created by tdzl2003 on 4/25/16.
 */

import React, {
  requireNativeComponent,
  PropTypes,
  View,
} from 'react-native'

const BDMapMarker = require('./BDMapMarker');

class BDMapView extends React.Component {
  state = {};
  static propTypes = {
    /** 继承自View的属性 **/
    ...View.propTypes,

    /** 选项 **/

    // 是否显示当前位置
    showsUserLocation: PropTypes.bool,
    blurMarkerWhileMove: PropTypes.bool,

    /** 属性 **/

    // 当前的视野区域
    region: React.PropTypes.shape({
      latitude: React.PropTypes.number.isRequired,
      longitude: React.PropTypes.number.isRequired,

      latitudeDelta: React.PropTypes.number,
      longitudeDelta: React.PropTypes.number,
    }),
    // 地图开始显示的视野区域.不指定region时才生效.
    defaultRegion: React.PropTypes.shape({
      latitude: React.PropTypes.number.isRequired,
      longitude: React.PropTypes.number.isRequired,

      latitudeDelta: React.PropTypes.number,
      longitudeDelta: React.PropTypes.number,
    }),
    annotations: React.PropTypes.arrayOf(React.PropTypes.shape({
      /**
       * The location of the annotation.
       */
      latitude: React.PropTypes.number.isRequired,
      longitude: React.PropTypes.number.isRequired,

      /**
       * Whether the pin should be draggable or not
       */
      draggable: React.PropTypes.bool,

      /**
       * Event that fires when the annotation drag state changes.
       */
      onDragStateChange: React.PropTypes.func,

      onPress: React.PropTypes.func,

      /**
       * Event that fires when the annotation gets was tapped by the user
       * and the callout view was displayed.
       */
      onFocus: React.PropTypes.func,

      /**
       * Event that fires when another annotation or the mapview itself
       * was tapped and a previously shown annotation will be closed.
       */
      onBlur: React.PropTypes.func,

      /**
       * Annotation title/subtile.
       */
      title: React.PropTypes.string,
      subtitle: React.PropTypes.string,

      /**
       * annotation id
       */
      id: React.PropTypes.string,

    })),

    // 事件
    onLoad: PropTypes.func,
    onRegionChange: PropTypes.func,
    onRegionChangeComplete: PropTypes.func,
  };
  static defaultProps = {
    showsUserLocation: false,
    blurMarkerWhileMove: false,
  };
  constructor(props){
    super(props);
    this._region =  props.region || props.defaultRegion;
  }
  componentDidMount(){
    if (this._region) {
      this._native.setNativeProps({
        region: this._region
      });
    }
  }
  componentWillReceiveProps(nextProps){
    if (!this._native) {
      return;
    }
    this.checkRegion(nextProps);
  }
  checkRegion(props){
    if (props.region && props.region !== this._region){
      this._region = props.region;
      if (this._region) {
        this._native.setNativeProps({
          region: this._region
        });
      }
    }
  }
  onRegionChange = ev => {
    const {onRegionChange, blurMarkerWhileMove} = this.props;

    this._region = ev.nativeEvent;
    onRegionChange && onRegionChange(ev.nativeEvent);
    if (blurMarkerWhileMove && this._focusMarker) {
      this._focusMarker.onBlur();
      this._focusMarker = null;
    }
    this.checkRegion(this.props);
  };
  onRegionChangeComplete = ev => {
    const {onRegionChange, onRegionChangeComplete} = this.props;
    this._region = ev.nativeEvent;
    onRegionChange && onRegionChange(ev.nativeEvent);
    onRegionChangeComplete && onRegionChangeComplete(ev.nativeEvent);
    this.checkRegion(this.props);
  };
  propsWithoutEvents(){
    const ret = {};
    for (const k in this.props){
      const v = this.props[k];
      if (typeof(v) !== 'function') {
        ret[k] = v;
      }
    }
    return ret;
  }
  renderMarker = anno => {
    return <BDMapMarker
          {...anno}
          key={anno.id}
          mapView={this}
        />;
  };
  onMarkerPress(marker) {
    if (this._focusMarker === marker) {
      return;
    }
    if (this._focusMarker) {
      this._focusMarker.onBlur();
    }
    this._focusMarker = marker;
    marker.onFocus();
    //this.forceUpdate();
  }
  onLoad = () => {
    const {onLoad} = this.props;
    onLoad && onLoad();
    this.setState({
      loaded: true,
    })
  };
  render() {
    const {annotations, children, region, ...others} = this.propsWithoutEvents();
    const {loaded} = this.state;

    //let infoWindow = null;
    //if (this._focusMarker) {
    //  infoWindow = this._focusMarker.renderInfoWindow();
    //}

    console.log(loaded, annotations);

    return (
      <RCTBDMapView
        {...others}
        ref={ref=>this._native = ref}
        onLoad={this.onLoad}
        onRegionChange={this.onRegionChange}
        onRegionChangeComplete={this.onRegionChangeComplete}
      >
        {loaded && annotations && annotations.map(this.renderMarker)}
        {/*infoWindow*/}
      </RCTBDMapView>
    );
  }
}

var RCTBDMapView = requireNativeComponent(`RCTBDMapView`, BDMapView);

module.exports = BDMapView;
