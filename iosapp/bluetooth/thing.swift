


import Mapbox
import UIKit
import Alamofire

class thing: UIViewController, MGLMapViewDelegate {
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let mapView = MGLMapView(frame: view.bounds)
        mapView.autoresizingMask = [.FlexibleWidth, .FlexibleHeight]
        mapView.styleURL = MGLStyle.darkStyleURLWithVersion(9)
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
        
        Alamofire.request(.GET, "https://dce96ee1.ngrok.io/data").responseJSON {response in
            
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
                    point.title = "\(diseases[x]) is predicted here"
                    pointAnnotations.append(point)
                    mapView.addAnnotation(point)
                }
                
            }
        }
        
    }
    
    
    
    // Fill an array with point annotations and add it to the map.
    
    /*func update () {
     dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0)) {
     dispatch_async(dispatch_get_main_queue()) {
     let mapView = MGLMapView(frame: self.view.bounds)
     
     var pointAnnotations = [MGLPointAnnotation]()
     var lats = [Double()]
     var longs = [Double()]
     var diseases = [String()]
     
     Alamofire.request(.GET, "https://dce96ee1.ngrok.io/data").responseJSON {response in
     
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
     point.title = "\(diseases[x])"
     pointAnnotations.append(point)
     mapView.addAnnotation(point)
     }
     
     }
     }
     }
     }
     }
     
     }*/
    
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
            let hue = CGFloat(annotation.coordinate.longitude) / 100
            annotationView!.backgroundColor = UIColor(hue: 0, saturation: 0.5, brightness: 1, alpha: 0.4)
        }
        
        return annotationView
    }
    
    func mapView(mapView: MGLMapView, annotationCanShowCallout annotation: MGLAnnotation) -> Bool {
        return true
    }
}