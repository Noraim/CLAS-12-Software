package org.jlab.rec.ft;

import java.util.List;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.ft.cal.FTCALConstantsLoader;


public class FTParticle {

	
	private int _ID;                                          // track ID
	private int _Charge;		         	          // 0/1 for photon/electron
	private double _Time;      			          // time of impact on the FT 
	private double _Energy;			                  // total energy of the cluster including correction
	private Vector3D _Position  = new Vector3D();             // position 
        private int _Cluster;					  // track pointer to cluster information in FTCALRec::cluster bank
	private int _Signal;					  // track pointer to signal information in FTHODORec::cluster bank
	private int _Cross;					  // track pointer to cross information in FTTRKRec::cluster bank
        private double _field;
	
	// constructor
	public FTParticle(int cid) {
		this.set_ID(cid);
	}


	public int get_ID() {
		return _ID;
	}


	public void set_ID(int _ID) {
		this._ID = _ID;
	}


	public int getCharge() {
		return _Charge;
	}


	public void setCharge(int _Charge) {
		this._Charge = _Charge;
	}
        

	public double getTime() {
		return _Time;
	}


	public void setTime(double _Time) {
		this._Time = _Time;
	}


	public double getEnergy() {
		return _Energy;
	}


	public void setEnergy(double _Energy) {
		this._Energy = _Energy;
	}

        public Vector3D getPosition() {
            return _Position;
        }

        public void setPosition(Vector3D _Position) {
            this._Position = _Position;
        }

        public double getField() {
            return _field;
        }

        public void setField(double _field) {
            this._field = _field;
        }

        public Vector3D getDirection() {
            Vector3D direction = new Vector3D();
            // for charged particle correct theta and phi of the impact point for the bend in the solenoid field according to the field scale
            if(this.getCharge()==-1) {
                double energy    = this.getEnergy();
                double thetaCorr = Math.exp(FTCALConstantsLoader.theta_corr[0]+FTCALConstantsLoader.theta_corr[1]*energy)+
			     	   Math.exp(FTCALConstantsLoader.theta_corr[2]+FTCALConstantsLoader.theta_corr[3]*energy);
                thetaCorr        = thetaCorr * this._field;
		double phiCorr   = Math.exp(FTCALConstantsLoader.phi_corr[0]+FTCALConstantsLoader.phi_corr[1]*energy)+
			     	   Math.exp(FTCALConstantsLoader.phi_corr[2]+FTCALConstantsLoader.phi_corr[3]*energy)+
			     	   Math.exp(FTCALConstantsLoader.phi_corr[4]+FTCALConstantsLoader.phi_corr[5]*energy);
                phiCorr          = phiCorr * this._field;
                direction.setMagThetaPhi(1, this.getPosition().theta()+thetaCorr, this.getPosition().phi()+phiCorr);
            }
            else {
                direction = this.getPosition().asUnit();
            }
            return direction;
        }

	public int getCalorimeterIndex() {
		return _Cluster;
	}


	public void setCalorimeterIndex(int _Cluster) {
		this._Cluster = _Cluster;
	}


	public int getHodoscopeIndex() {
		return _Signal;
	}


	public void setHodoscopeIndex(int _Signal) {
		this._Signal = _Signal;
	}


	public int getTrackerIndex() {
		return _Cross;
	}


	public void setTrackerIndex(int _Cross) {
		this._Cross = _Cross;
	}

        public int getDetectorHit(List<FTResponse>  hitList, String detectorType, double distanceThreshold, double timeThresholds){
        
            Vector3D  hitPoint     = this.getPosition().clone();
            Vector3D  matchedPoint = new Vector3D();
            double   minimumDistance = 500.0;
            int      bestIndex       = -1;
            for(int loop = 0; loop < hitList.size(); loop++){
                FTResponse response = hitList.get(loop);
                if(response.getAssociation()<0 && response.getType() == detectorType){
                    matchedPoint.setXYZ(
                            response.getPosition().x(),
                            response.getPosition().y(),
                            response.getPosition().z()
                            );
                    double hitdistance = hitPoint.sub(matchedPoint).mag();
                    double timedistance = this.getTime()-response.getTime();
                    //System.out.println(" LOOP = " + loop + "   distance = " + hitdistance);
                    if(timedistance<timeThresholds&&hitdistance<distanceThreshold&&hitdistance<minimumDistance){
                        minimumDistance = hitdistance;
                        bestIndex       = loop;
                    }
                }
            }
            return bestIndex;
        }
        
        public void show() {
            System.out.println( "FT Particle info " +
                                " Charge = "+ this.getCharge() +
                                " E = "     + this.getEnergy() +
                                " Theta = " + Math.toDegrees(this.getDirection().theta()) +
                                " Phi = "   + Math.toDegrees(this.getDirection().phi()) +
                                " Time = "  + this.getTime());
        }
}
