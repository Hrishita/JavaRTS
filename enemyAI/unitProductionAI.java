package enemyAI;

import core.baseInfo;
import core.gameData;
import core.mainThread;
import core.vector;
import entity.*;

//decide which unit to produce to counter player's force
//keep track of the units that are under control by combatAI.

public class unitProductionAI {
	
	public baseInfo theBaseInfo;
	
	public lightTank[] lightTanksControlledByCombatAI;
	public rocketTank[] rocketTanksControlledByCombatAI;
	public stealthTank[] stealthTanksControlledByCombatAI;
	public heavyTank[] heavyTanksControlledByCombatAI;
	public solidObject[] troopsControlledByCombatAI;
	
	public float combatAICenterX;
	public float combatAICenterZ;
	
	//public int currentState;
	//public final int booming = 0;
	//public final int aggressing = 1;
	//public final int defending = 2;
	
	public int currentProductionOrder;
	public final int produceLightTank = 0;
	public final int produceRocketTank = 1;
	public final int produceStealthTank = 2;
	public final int produceHeavyTank = 3;
	
	public vector rallyPoint;
	//public int unitProduced;
	public int numberOfCombatUnit;
	public int numberOfUnitInCombatRadius;
	public int numberOfUnitOutsideCombatRadius;
	public int numberOfCombatUnitsUnderAttack;
	
	public int numberOfLightTanksControlledByCombatAI;
	public int numberOfRocketTanksControlledByCombatAI;
	public int numberOfStealthTanksControlledByCombatAI;
	public int numberOfHeavyTanksControlledByCombatAI;
	
	public solidObject[] unitInCombatRadius;
	public solidObject[] unitOutsideCombatRadius;
	
	public int frameAI;
	
	public unitProductionAI(baseInfo theBaseInfo){
		this.theBaseInfo = theBaseInfo;
		rallyPoint = new vector(0,0,0);
		
		lightTanksControlledByCombatAI = new lightTank[192];
		rocketTanksControlledByCombatAI = new rocketTank[72];
		stealthTanksControlledByCombatAI = new stealthTank[96];
		heavyTanksControlledByCombatAI = new heavyTank[60];
		
		troopsControlledByCombatAI = new solidObject[512];
		unitInCombatRadius = new solidObject[384];
		unitOutsideCombatRadius = new solidObject[128];
		
		combatAICenterX = -1;
		combatAICenterZ = -1;
	
	}
	

	
	public void processAI(){
		frameAI++;
		
		//set the rally point to near the construction yard which is closest to the AI player's starting position
		float x = 0;
		float z = 999999;
		
		
		int numberOfLightTanks_AI = mainThread.ec.theUnitProductionAI.numberOfLightTanksControlledByCombatAI;
		int numberOfRocketTanks_AI = mainThread.ec.theUnitProductionAI.numberOfRocketTanksControlledByCombatAI;
		int numberOfStealthTanks_AI = mainThread.ec.theUnitProductionAI.numberOfStealthTanksControlledByCombatAI;
		int numberOfHeavyTanks_AI = mainThread.ec.theUnitProductionAI.numberOfHeavyTanksControlledByCombatAI;
		boolean unitCountLow = mainThread.ec.theCombatManagerAI.unitCountLow;
		
		int index = 0;
		for(int i = 0; i < mainThread.theAssetManager.constructionYards.length; i++){
			if(mainThread.theAssetManager.constructionYards[i] != null && mainThread.theAssetManager.constructionYards[i].currentHP > 0 &&  mainThread.theAssetManager.constructionYards[i].teamNo != 0){
				if(unitCountLow && mainThread.ec.theDefenseManagerAI.majorThreatLocation.x != 0) {
					float xPos1 = mainThread.theAssetManager.constructionYards[i].centre.x;
					float zPos1 = mainThread.theAssetManager.constructionYards[i].centre.z;
					float xPos2 = mainThread.ec.theDefenseManagerAI.majorThreatLocation.x;
					float zPos2 = mainThread.ec.theDefenseManagerAI.majorThreatLocation.z;
					float d = (xPos1 - xPos2) * (xPos1 - xPos2) + (zPos1 - zPos2) * (zPos1 - zPos2);
					if(d < 9) {
						continue;
					}
				}
				
				index = i;
				if(mainThread.theAssetManager.constructionYards[i].centre.z < z && mainThread.theAssetManager.constructionYards[i].centre.z > 7 && mainThread.theAssetManager.constructionYards[i].centre.x > 7){
					x = mainThread.theAssetManager.constructionYards[i].centre.x;
					z = mainThread.theAssetManager.constructionYards[i].centre.z;
				}
			}
		}
		if(z != 999999) {
			if(mainThread.ec.theCombatManagerAI.shouldDefenceAggressively)
				rallyPoint.set(z - 2.5f, 0, z - 2.5f);
			else
				rallyPoint.set(x - 1.5f, 0, z - 1.5f);
		}else {
			if(mainThread.theAssetManager.constructionYards[index] != null && mainThread.theAssetManager.constructionYards[index].teamNo !=0)
				rallyPoint.set(mainThread.theAssetManager.constructionYards[index].centre.x - 2.5f, 0,  mainThread.theAssetManager.constructionYards[index].centre.z -2.5f);
		}
		
		
		
		//make sure not to over produce when the resource is running low
		int maxNumOfUnitCanBeProduced =  theBaseInfo.currentCredit / 500 + 1;
		
		
		for(int i = 0; i < mainThread.theAssetManager.factories.length; i++){
			factory f = mainThread.theAssetManager.factories[i];
			if(f != null && f.teamNo !=0){
				if(!f.isIdle())
					maxNumOfUnitCanBeProduced--;
			}
		}
		
		for(int i = 0; i < mainThread.theAssetManager.constructionYards.length; i++){
			constructionYard c = mainThread.theAssetManager.constructionYards[i];
			if(c != null && c.teamNo !=0){
				if(!c.isIdle())
					maxNumOfUnitCanBeProduced--;
			}
		}
		
		
		//make decision on what unit to produce
		int numberOfPlayerGunTurrets=   mainThread.ec.theMapAwarenessAI.numberOfGunTurret_player;
		int numberOfPlayerMissileTurrets=  mainThread.ec.theMapAwarenessAI.numberOfMissileTurret_player;
		int numberOfLightTanks_player = mainThread.ec.theMapAwarenessAI.numberOfLightTanks_player;
		int numberOfRocketTanks_player = mainThread.ec.theMapAwarenessAI.numberOfRocketTanks_player;
		int numberOfStealthTanks_player = mainThread.ec.theMapAwarenessAI.numberOfStealthTanks_player;
		int numberOfHeavyTanks_player = mainThread.ec.theMapAwarenessAI.numberOfHeavyTanks_player;
		int maxNumberOfStealthTanks_playerInLastFiveMinutes =  mainThread.ec.theMapAwarenessAI.maxNumberOfStealthTanks_playerInLastFiveMinutes;
		
		boolean playerHasMostlyLightTanks = mainThread.ec.theMapAwarenessAI.playerHasMostlyLightTanks;
		boolean playerHasMostlyHeavyTanks =  mainThread.ec.theMapAwarenessAI.playerHasMostlyHeavyTanks;
		boolean playIsRushingHighTierUnits = mainThread.ec.theMapAwarenessAI.playIsRushingHighTierUnits;
		boolean playerLikelyCanNotProduceHighTierUnits = mainThread.ec.theMapAwarenessAI.playerLikelyCanNotProduceHighTierUnits;
		boolean playerDoesntHaveMassHeavyTanks = mainThread.ec.theMapAwarenessAI.playerDoesntHaveMassHeavyTanks;
		boolean playerHasManyLightTanksButNoHeavyTank = mainThread.ec.theMapAwarenessAI.playerHasManyLightTanksButNoHeavyTank;
		boolean playerHasMostlyHeavyAndStealthTanks = mainThread.ec.theMapAwarenessAI.playerHasMostlyHeavyAndStealthTanks;
		
		if((numberOfRocketTanks_AI < 2 &&  frameAI > 300 ) || numberOfRocketTanks_AI < numberOfPlayerGunTurrets + numberOfPlayerMissileTurrets*1.5 || (gameData.getRandom() > 925 && !playerHasMostlyLightTanks)){
			currentProductionOrder = produceRocketTank;
		}else if(theBaseInfo.canBuildHeavyTank &&
				playerHasMostlyHeavyAndStealthTanks || 
				 (!playerHasManyLightTanksButNoHeavyTank
				 && !playerHasMostlyLightTanks 
				 && !(numberOfHeavyTanks_player == 0 && maxNumberOfStealthTanks_playerInLastFiveMinutes < 3 &&  mainThread.frameIndex/30 > 600)  
				 && !(playerHasMostlyHeavyTanks && numberOfStealthTanks_player < numberOfHeavyTanks_AI*2) 
				 && (playIsRushingHighTierUnits || gameData.getRandom() > 985 ||  maxNumberOfStealthTanks_playerInLastFiveMinutes*4 > numberOfHeavyTanks_AI  || (mainThread.frameIndex/30 > 400 && mainThread.frameIndex/30 < 600 &&  numberOfPlayerGunTurrets +  numberOfPlayerMissileTurrets+ numberOfLightTanks_player + numberOfRocketTanks_player + numberOfHeavyTanks_player*5 < 5)))){
			currentProductionOrder = produceHeavyTank; 
		}else if(theBaseInfo.canBuildStealthTank && (playerHasMostlyLightTanks || playerLikelyCanNotProduceHighTierUnits || playerDoesntHaveMassHeavyTanks) && !playerHasMostlyHeavyTanks){
			currentProductionOrder = produceStealthTank;
		}else{
			currentProductionOrder = produceLightTank;
		}
		
		
		
		//make decision on what tech to research
		if(mainThread.ec.theBuildingManagerAI.theBaseInfo.numberOfCommunicationCenter > 0) {
			if(mainThread.ec.theDefenseManagerAI.needMissileTurret || theBaseInfo.currentCredit > 1500) {
				if(!communicationCenter.rapidfireResearched_enemy) {
					if(communicationCenter.rapidfireResearchProgress_enemy == 255){
						communicationCenter.researchRapidfire(1);
						System.out.println("----------------------------AI starts researching rapid fire ability------------------------------------");
					}
				}
			}
		}
		
		if(mainThread.ec.theBuildingManagerAI.theBaseInfo.numberOfTechCenter > 0){	
					
			
			if(currentProductionOrder == produceStealthTank)
				System.out.println("should make stealth tank now--------------");
			if(currentProductionOrder == produceHeavyTank)
				System.out.println("should make Heavy tank now-----------------");
			if(currentProductionOrder == produceRocketTank)
				System.out.println("should make Rocket tank now----------------");
			
			
			//Immediately  start  stealth tank upgrades  when a tech center is built
			if(!techCenter.stealthTankResearched_enemy){
				if(techCenter.stealthTankResearchProgress_enemy == 255 && ((numberOfLightTanks_player + numberOfStealthTanks_player> 8) ||  theBaseInfo.currentCredit > 2000 || numberOfStealthTanks_AI > 6)){
					techCenter.cancelResearch(1);
					techCenter.researchStealthTank(1);
					System.out.println("----------------------------AI starts researching stealth tank------------------------------------");
				}
			}
		
			
			if(numberOfLightTanks_AI >= 15  && theBaseInfo.currentCredit > 1000){
				if(!techCenter.lightTankResearched_enemy){
					if(techCenter.lightTankResearchProgress_enemy >= 240 && techCenter.stealthTankResearchProgress_enemy >= 240 && techCenter.rocketTankResearchProgress_enemy >= 240 && techCenter.heavyTankResearchProgress_enemy >= 240){
						techCenter.researchLightTank(1);
						System.out.println("----------------------------AI starts researching light tank------------------------------------");
					}
				}
			}
			
			if(numberOfRocketTanks_AI > 2 && theBaseInfo.currentCredit > 750 && (numberOfPlayerGunTurrets > 0 || numberOfPlayerMissileTurrets > 0)){
				if(!techCenter.rocketTankResearched_enemy){
					if(techCenter.lightTankResearchProgress_enemy >= 240 && techCenter.stealthTankResearchProgress_enemy >= 240 && techCenter.rocketTankResearchProgress_enemy >= 240 && techCenter.heavyTankResearchProgress_enemy >= 240){

						techCenter.researchRocketTank(1);
						System.out.println("----------------------------AI starts researching rocket tank------------------------------------");
					}
				}
			}
			
			if(numberOfHeavyTanks_AI > 5 && theBaseInfo.currentCredit > 1000){
				if(!techCenter.heavyTankResearched_enemy){
					if(techCenter.lightTankResearchProgress_enemy >= 240 && techCenter.stealthTankResearchProgress_enemy >= 240 && techCenter.rocketTankResearchProgress_enemy >= 240 && techCenter.heavyTankResearchProgress_enemy >= 240){
						techCenter.researchHeavyTank(1);
						System.out.println("----------------------------AI starts researching heavy tank------------------------------------");
					}
				}
			}
			
			
		}
		
	
		for(int i = 0; i < mainThread.theAssetManager.factories.length; i++){
			factory f = mainThread.theAssetManager.factories[i];
			if(f != null && f.teamNo !=0){
				f.moveTo(rallyPoint.x, rallyPoint.z);
				if(f.isIdle()){
					if(theBaseInfo.canBuildLightTank && maxNumOfUnitCanBeProduced > 0){
						
						if(currentProductionOrder == produceLightTank)
							f.buildLightTank();
						else if(currentProductionOrder == produceRocketTank)
							f.buildRocketTank();
						else if(currentProductionOrder == produceStealthTank)
							f.buildStealthTank();
						else if(currentProductionOrder == produceHeavyTank)
							f.buildHeavyTank();
						
						maxNumOfUnitCanBeProduced--;
					}
					continue;
				}
			}
		}
		
		
		countTroopControlledByCombatAI();
		findCenterOfTroopControlledByCombatAI();

	}
	
	public void addLightTank(lightTank o){
		//check if other AI agent need light tank
		
		if(mainThread.ec.theScoutingManagerAI.needLightTank()){
			mainThread.ec.theScoutingManagerAI.addLightTank(o);
			
			return;
		}
		
		
		//add the new light tank to combat AI's command
		for(int i = 0; i < lightTanksControlledByCombatAI.length; i++){
			if(lightTanksControlledByCombatAI[i] == null || (lightTanksControlledByCombatAI[i] != null && lightTanksControlledByCombatAI[i].currentHP <=0)){
				lightTanksControlledByCombatAI[i] = o;
				mainThread.ec.theDefenseManagerAI.addUnitToDefenders(o);
				break;
			}
		}
		
		
	}
	
	public void addRocketTank(rocketTank o){
		//check if other AI agent need rocket tank
		
		//add the new rocket tank to combat AI's command
		for(int i = 0; i < rocketTanksControlledByCombatAI.length; i++){
			if(rocketTanksControlledByCombatAI[i] == null || (rocketTanksControlledByCombatAI[i] != null && rocketTanksControlledByCombatAI[i].currentHP <=0)){
				rocketTanksControlledByCombatAI[i] = o;
				break;
			}
		}
	}
	
	public void addStealthTank(stealthTank o){
		//check if other AI agent need stealth tank
		
		if(mainThread.ec.theScoutingManagerAI.needStealthTank()){
			mainThread.ec.theScoutingManagerAI.addStealthTank(o);
			return;
		}
		
		if(mainThread.ec.theBaseExpentionAI.needStealthTank()){
			mainThread.ec.theBaseExpentionAI.addStealthTank(o);
			return;
		}
		
		
		
		//add the new stealth tank to combat AI's command
		for(int i = 0; i < stealthTanksControlledByCombatAI.length; i++){
			if(stealthTanksControlledByCombatAI[i] == null || (stealthTanksControlledByCombatAI[i] != null && stealthTanksControlledByCombatAI[i].currentHP <=0)){
				stealthTanksControlledByCombatAI[i] = o;
			
				mainThread.ec.theDefenseManagerAI.addUnitToDefenders(o);
				break;
			}
		}
	}
	
	public void addHeavyTank(heavyTank o){
		//add the new heavy tank to combat AI's command
		for(int i = 0; i < heavyTanksControlledByCombatAI.length; i++){
			if(heavyTanksControlledByCombatAI[i] == null || (heavyTanksControlledByCombatAI[i] != null && heavyTanksControlledByCombatAI[i].currentHP <=0)){
				heavyTanksControlledByCombatAI[i] = o;
				break;
			}
		}
	}
	
	
	public void countTroopControlledByCombatAI(){
		numberOfCombatUnitsUnderAttack = 0;
		
		numberOfLightTanksControlledByCombatAI = 0;
		numberOfRocketTanksControlledByCombatAI = 0;
		numberOfStealthTanksControlledByCombatAI = 0;
		numberOfHeavyTanksControlledByCombatAI = 0;
		
		for(int i = 0; i < troopsControlledByCombatAI.length; i++){
			troopsControlledByCombatAI[i] = null;
		}
		
		numberOfCombatUnit = 0;
		for(int i = 0; i < lightTanksControlledByCombatAI.length; i++){
			if(lightTanksControlledByCombatAI[i] != null && lightTanksControlledByCombatAI[i].currentHP > 0){
				troopsControlledByCombatAI[numberOfCombatUnit] = lightTanksControlledByCombatAI[i];
				if(troopsControlledByCombatAI[numberOfCombatUnit].underAttackCountDown > 0)
					numberOfCombatUnitsUnderAttack++;
				numberOfCombatUnit++;
				numberOfLightTanksControlledByCombatAI++;
			}
		}
		for(int i = 0; i < rocketTanksControlledByCombatAI.length; i++){
			if(rocketTanksControlledByCombatAI[i] != null && rocketTanksControlledByCombatAI[i].currentHP > 0){
				troopsControlledByCombatAI[numberOfCombatUnit] = rocketTanksControlledByCombatAI[i];
				if(troopsControlledByCombatAI[numberOfCombatUnit].underAttackCountDown > 0)
					numberOfCombatUnitsUnderAttack++;
				numberOfCombatUnit++;
				numberOfRocketTanksControlledByCombatAI++;
			}
		}
		for(int i = 0; i < stealthTanksControlledByCombatAI.length; i++){
			if(stealthTanksControlledByCombatAI[i] != null && stealthTanksControlledByCombatAI[i].currentHP > 0){
				troopsControlledByCombatAI[numberOfCombatUnit] = stealthTanksControlledByCombatAI[i];
				if(troopsControlledByCombatAI[numberOfCombatUnit].underAttackCountDown > 0)
					numberOfCombatUnitsUnderAttack++;
				numberOfCombatUnit++;
				numberOfStealthTanksControlledByCombatAI++;
			}
		}
		for(int i = 0; i < heavyTanksControlledByCombatAI.length; i++){
			if(heavyTanksControlledByCombatAI[i] != null && heavyTanksControlledByCombatAI[i].currentHP > 0){
				troopsControlledByCombatAI[numberOfCombatUnit] = heavyTanksControlledByCombatAI[i];
				if(troopsControlledByCombatAI[numberOfCombatUnit].underAttackCountDown > 0)
					numberOfCombatUnitsUnderAttack++;
				numberOfCombatUnit++;
				numberOfHeavyTanksControlledByCombatAI++;
			}
		}
	}
	
	public void findCenterOfTroopControlledByCombatAI(){
		float centerX = 0;
		float centerZ = 0;
		
		vector centre;
		double distance = 0;
		
		numberOfUnitInCombatRadius = 0;
		numberOfUnitOutsideCombatRadius = 0;
		
		
		//when there is no combat unit there is no point to calculate the center of the combat units
		if(numberOfCombatUnit == 0){
			combatAICenterX = -1;
			combatAICenterZ = -1;
			return;
		}
		
		for(int i = 0; i <  unitInCombatRadius.length; i++){
			unitInCombatRadius[i] = null;
		}
		
		for(int i = 0; i <  unitOutsideCombatRadius.length; i++){
			unitOutsideCombatRadius[i] = null;
		}
		
		
		//calculate the center of the troops using the all the unites that are under the control of the combatAI
		if(combatAICenterX == -1){
			combatAICenterX = 0;
			combatAICenterZ = 0;
			for(int i =0; i < numberOfCombatUnit; i++){
				centre = troopsControlledByCombatAI[i].centre;
				combatAICenterX+=centre.x;
				combatAICenterZ+=centre.z;
			}
			combatAICenterX /= numberOfCombatUnit;
			combatAICenterZ /= numberOfCombatUnit;
		}
		
		//exclude the units are too far away from the center of the troops, (i.e the unites that just come out of the factory), and recalculate the center
		for(int i =0; i < numberOfCombatUnit; i++){
			centre = troopsControlledByCombatAI[i].centre;
			distance = Math.sqrt((centre.x - combatAICenterX)*(centre.x - combatAICenterX) + (centre.z - combatAICenterZ)*(centre.z - combatAICenterZ));
			if(distance < 4.5){
				centerX += centre.x;
				centerZ += centre.z;
				if(numberOfUnitInCombatRadius < unitInCombatRadius.length)
					unitInCombatRadius[numberOfUnitInCombatRadius] = troopsControlledByCombatAI[i];
				numberOfUnitInCombatRadius++;
			}else{
				if(numberOfUnitOutsideCombatRadius < unitOutsideCombatRadius.length)
					unitOutsideCombatRadius[numberOfUnitOutsideCombatRadius] = troopsControlledByCombatAI[i];
				numberOfUnitOutsideCombatRadius++;
			}
		}
		
		float unitInCombactRadiusPercentage = 1;
		if(numberOfUnitInCombatRadius + numberOfUnitOutsideCombatRadius >0)
			unitInCombactRadiusPercentage = (float)numberOfUnitInCombatRadius/(float)(numberOfUnitInCombatRadius + numberOfUnitOutsideCombatRadius);
		
		float unitInCombactRadiusPercentageThreshold = 0.7f;
		if(mainThread.ec.theCombatManagerAI.currentState == mainThread.ec.theCombatManagerAI.aggressing) {
			if(mainThread.ec.theCombatManagerAI.distanceToTarget < 6)
				unitInCombactRadiusPercentageThreshold = 0.475f;
		}
		if(numberOfCombatUnitsUnderAttack > 0)
			unitInCombactRadiusPercentageThreshold = 0.25f;
		
		//need to recalculate the center  if  there is a significant amount of combat unites that are outside of the combat radius 
		if(unitInCombactRadiusPercentage < unitInCombactRadiusPercentageThreshold){
			
			combatAICenterX = 0;
			combatAICenterZ = 0;
			for(int i =0; i < numberOfCombatUnit; i++){
				centre = troopsControlledByCombatAI[i].centre;
				combatAICenterX+=centre.x;
				combatAICenterZ+=centre.z;
			}
			combatAICenterX /= numberOfCombatUnit;
			combatAICenterZ /= numberOfCombatUnit;
			
			for(int i = 0; i <  unitInCombatRadius.length; i++){
				unitInCombatRadius[i] = null;
			}
			
			for(int i = 0; i <  unitOutsideCombatRadius.length; i++){
				unitOutsideCombatRadius[i] = null;
			}
			numberOfUnitInCombatRadius = 0;
			numberOfUnitOutsideCombatRadius = 0;
			centerX = 0;
			centerZ = 0;
			
			for(int i =0; i < numberOfCombatUnit; i++){
				centre = troopsControlledByCombatAI[i].centre;
				distance = Math.sqrt((centre.x - combatAICenterX)*(centre.x - combatAICenterX) + (centre.z - combatAICenterZ)*(centre.z - combatAICenterZ));
				if(distance < 10){
					centerX += centre.x;
					centerZ += centre.z;
					if(numberOfUnitInCombatRadius < unitInCombatRadius.length)
						unitInCombatRadius[numberOfUnitInCombatRadius] = troopsControlledByCombatAI[i];
					numberOfUnitInCombatRadius++;
				}else{
					if(numberOfUnitOutsideCombatRadius < unitOutsideCombatRadius.length)
						unitOutsideCombatRadius[numberOfUnitOutsideCombatRadius] = troopsControlledByCombatAI[i];
					numberOfUnitOutsideCombatRadius++;
				}
			}
		}
		
		combatAICenterX = centerX/numberOfUnitInCombatRadius;
		combatAICenterZ = centerZ/numberOfUnitInCombatRadius;
		
	}
}
