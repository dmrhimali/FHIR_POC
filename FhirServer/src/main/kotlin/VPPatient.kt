import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.StringType
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.io.Serializable

@Document(indexName = "vp_members")
data class VPPatient(
        @Id
        var id: String,
        @Field(type = FieldType.Text, name = "birthDate")
        val birthDate: String? = null,
        var address: List<Address>? = listOf(),
        val gender: String? = null,
        var name: List<Name>? = listOf(),
        @Field(type = FieldType.Keyword, name = "resourceType")
        val resourceType: String?,
) : Serializable {
    override fun toString(): String = "id = $id" +
            "birthDate - $birthDate" +
            "address = $address" +
            "gender = $gender" +
            "name  = $name" +
            "resourceType = $resourceType"
}

data class Name(
        val family: String? = null,
        val given: List<String>? = listOf(),
        val use: String? = null
) : Serializable

data class Address(
    val city: String? = null,
    val district: String? = null,
    val line: List<String>? = null,
    val postalCode: String? = null,
    val state: String? = null,
    val text: String? = null,
    val type: String? = null,
    val use: String? = null
) : Serializable


fun VPPatient.mapToFhirPatient(): Patient {
    val patient = Patient()
    patient.id = this.id

    this.name?.forEach { name ->
        patient.addName().family = name.family

        patient.addName().given = name.given?.map { given ->
            StringType(given)
        }
    }

    this.address?.forEach { address ->
        address.line?.forEach {
            patient.addAddress().addLine(it.toString())
        }

        patient.addAddress().city = address.city
        patient.addAddress().state = address.state
        patient.addAddress().postalCode = address.postalCode
    }

    return patient
}

fun VPPatient.mapFromFhirPatient(patient: Patient): VPPatient {
    this.id = patient.id

    val nameList = mutableListOf<Name>()
    patient.name.forEach { name ->
        nameList.add(Name(
            family = name.family,
            given = name.given.map { it -> it.value }
        ))
    }
    this.name = nameList

    val addressList = mutableListOf<Address>()
    patient.address.forEach { address ->
        addressList.add(Address(
                city = address.city,
                district = address.district,
                line = address.line.map { it.value }
        ))
    }
    this.address = addressList

    return this
}
