package basics;

/**
 * Class YAGO - YAGO2S
 * 
 * Contains YAGO identifiers
 * 
 * @author Fabian M. Suchanek
 */

public class YAGO {

	public static final String function=FactComponent.forYagoEntity("yagoFunction");
	public static final String string="xsd:string";
	public static final String yago=FactComponent.forYagoEntity("YAGO");
	public static final String hasNumber=FactComponent.forYagoEntity("hasNumber");
	public static final String entity = "owl:Thing";//FactComponent.forWordnetEntity("entity", "100001740");
	public static final String person="<wordnet_person_100007846>";
	public static final String organization="<wordnet_organization_108008335>";
	public static final String location="<yagoGeoEntity>";
	public static final String artifact="<wordnet_artifact_100021939>";
	public static final String physicalEntity="<wordnet_physical_entity_100001930>";
	public static final String abstraction="<wordnet_abstraction_100002137>";
	public static final String extractionSource="<extractionSource>";
	public static final String extractionTechnique="<extractionTechnique>";
  public static final String languageString = "<yagoLanString>";
  public static final String hasConfidence = "<hasConfidence>";
  public static final String building = "<wordnet_building_102913152>";
  public static final String hasGloss = "<hasGloss>";
}
