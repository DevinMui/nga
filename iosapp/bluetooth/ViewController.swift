




import Mapbox
import UIKit
import Alamofire

class ViewController: UIViewController, MGLMapViewDelegate, CLLocationManagerDelegate {
    
    let locationManager = CLLocationManager()
    let email = NSUserDefaults.standardUserDefaults().stringForKey("email")
    
    var longitude = -122.0124813449774
    var latitude = 37.38375283781195
    
    var latt = Double()
    var longg = Double()

    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.locationManager.requestAlwaysAuthorization()
        
        // For use in foreground
        self.locationManager.requestWhenInUseAuthorization()
        
        if CLLocationManager.locationServicesEnabled() {
            locationManager.delegate = self
            locationManager.desiredAccuracy = kCLLocationAccuracyBest
            locationManager.startUpdatingLocation()
        }
        
        _ = NSTimer.scheduledTimerWithTimeInterval(2.5, target: self, selector: Selector("update"), userInfo: nil, repeats: true)
        
        let mapView = MGLMapView(frame: view.bounds)
        mapView.autoresizingMask = [.FlexibleWidth, .FlexibleHeight]
        mapView.centerCoordinate = CLLocationCoordinate2DMake(37.3798862, -122.01248270000002)
        mapView.showsUserLocation = true
        mapView.zoomLevel = 10
        mapView.maximumZoomLevel = 15
        mapView.delegate = self
        view.addSubview(mapView)
        
        var pointAnnotations = [MGLPointAnnotation]()
        var lats = [Double()]
        var longs = [Double()]
        var diseases = [String()]
        
        lats.removeAll()
        longs.removeAll()
        diseases.removeAll()
        
        Alamofire.request(.GET, "http://dce96ee1.ngrok.io/data").responseJSON {response in
            
            if let JSON = response.result.value {
                
                for i in 0..<JSON.count {
                    
                    let long = JSON[i]["long"] as! Double
                    let lat = JSON[i]["lat"] as! Double
                    let dis = JSON[i]["disease"] as! String
                    lats.append(lat)
                    longs.append(long)
                    diseases.append(dis)
                    
                }
                
                for x in 0..<longs.count {
                    let point = MGLPointAnnotation()
                    point.coordinate = CLLocationCoordinate2DMake(lats[x], longs[x])
                    print(longs[x])
                    point.title = "\(diseases[x]) has been reported here"

                    pointAnnotations.append(point)
                    mapView.addAnnotation(point)
                }
                
            }
        }
        
    }
    
    let headers : [String : String] = [
        "Accept": "application/json",
        "Content-Type": "application/json"
    ]

    
    func locationManager(manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        let locValue:CLLocationCoordinate2D = manager.location!.coordinate
        
        let lat = locValue.latitude as Double
        let long = locValue.longitude as Double

        longg = long
        latt = lat
        
        //print("locations = \(locValue.latitude) \(locValue.longitude)")
    }
    
        // Fill an array with point annotations and add it to the map.

    func update () {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0)) {
            dispatch_async(dispatch_get_main_queue()) {
                
                let headers : [String : String] = [
                    "Accept": "application/json",
                    "Content-Type": "application/json"
                ]
                
                let params = [
                    "lat": self.latitude,
                    "long": self.longitude,
                    "user_email": self.email!,
                    ]
                if self.longg != self.longitude || self.latt != self.latitude {
                    
                    self.longitude = self.longg
                    self.latitude = self.latt
                    
                    Alamofire.request(.POST, "http://dce96ee1.ngrok.io/data", parameters: params as? [String : AnyObject], headers: headers, encoding: .JSON)
                        .responseJSON { response in
                            if let JSON = response.result.value {
                                print(JSON)
                            }
                    }
                }
            }
        }
    
    }

    // MARK: - MGLMapViewDelegate methods
    
    // This delegate method is where you tell the map to load a view for a specific annotation. To load a static MGLAnnotationImage, you would use `-mapView:imageForAnnotation:`.
    func mapView(mapView: MGLMapView, viewForAnnotation annotation: MGLAnnotation) -> MGLAnnotationView? {
        // This example is only concerned with point annotations.
        guard annotation is MGLPointAnnotation else {
            return nil
        }
        
        // Use the point annotation’s longitude value (as a string) as the reuse identifier for its view.
        let reuseIdentifier = "\(annotation.coordinate.longitude)"
        
        // For better performance, always try to reuse existing annotations.
        var annotationView = mapView.dequeueReusableAnnotationViewWithIdentifier(reuseIdentifier)
        
        // If there’s no reusable annotation view available, initialize a new one.
        if annotationView == nil {
            annotationView = CustomAnnotationView(reuseIdentifier: reuseIdentifier)
            annotationView!.frame = CGRectMake(0, 0, 60, 60)
            
            // Set the annotation view’s background color to a value determined by its longitude.
            annotationView!.backgroundColor = UIColor(hue: 0, saturation: 0.5, brightness: 1, alpha: 0.4)
        }
        
        return annotationView
    }
    
    func mapView(mapView: MGLMapView, annotationCanShowCallout annotation: MGLAnnotation) -> Bool {
        return true
    }
}

//
// MGLAnnotationView subclass
class CustomAnnotationView: MGLAnnotationView {
    override func layoutSubviews() {
        super.layoutSubviews()
        
        // Force the annotation view to maintain a constant size when the map is tilted.
        scalesWithViewingDistance = false
        
        // Use CALayer’s corner radius to turn this view into a circle.
        layer.cornerRadius = frame.width / 2
    }
    
}