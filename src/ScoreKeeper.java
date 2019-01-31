/**
 * Initialise et met à jour le score d'un nombre de joueurs défini à la création.
 */
public class ScoreKeeper {

    private int[] scoreList;

ScoreKeeper(int numberOfPlayers){
    scoreList = new int[numberOfPlayers];

    for(int i = 0; i<numberOfPlayers; i++){
        scoreList[i] = 0;
    }
 }

    public int[] getScoreList() {
        return scoreList;
    }

    public void setScoreList(int[] scoreList) {
        this.scoreList = scoreList;
    }

    /**
     * incrémente le score du joueur d'id playerId
     * @param playerId
     */
    public void AddOneToScore(int playerId){
    this.scoreList[playerId]++;
 }

    public int[] DeclareVictor(){
        int victorId = 0;
        int highestScore = scoreList[0];

        for(int i = 1; i < scoreList.length; i++){

            if(scoreList[i] > highestScore){
                victorId = i;
                highestScore = scoreList[i];
            }

        }

        int[] res = new int[2];
        res[0] = victorId;
        res[1] = highestScore;
        return res;
    }

}
