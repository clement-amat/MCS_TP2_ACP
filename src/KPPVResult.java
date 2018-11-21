public class KPPVResult implements Comparable<KPPVResult> {
    public double distance;
    public int indiceFichierAP;

    /**
     *
     * @param distance
     * @param indiceFichierAP
     */
    public KPPVResult(double distance, int indiceFichierAP) {
        this.distance = distance;
        this.indiceFichierAP = indiceFichierAP;
    }

    public int compareTo(KPPVResult o) {
        if (this.distance == o.distance) {
            return Integer.compare(indiceFichierAP, o.indiceFichierAP);
        }
        return Double.compare(distance, o.distance);
    }
}
