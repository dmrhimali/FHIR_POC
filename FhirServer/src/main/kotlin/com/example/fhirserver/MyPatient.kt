package com.example.fhirserver

import ca.uhn.fhir.model.api.annotation.Child
import ca.uhn.fhir.model.api.annotation.Description
import ca.uhn.fhir.model.api.annotation.Extension
import ca.uhn.fhir.model.api.annotation.ResourceDef
import ca.uhn.fhir.util.ElementUtil
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.StringType


@ResourceDef(name = "Patient", profile = "http://example.com/StructureDefinition/mypatient")
class MyPatient : Patient() {
    /**
     * Each extension is defined in a field. Any valid HAPI Data Type
     * can be used for the field type. Note that the [name=""] attribute
     * in the @Child annotation needs to match the name for the bean accessor
     * and mutator methods.
     */
    @Child(name = "petName")
    @Extension(url = "http://example.com/dontuse#petname", definedLocally = false, isModifier = false)
    @Description(shortDefinition = "The name of the patient's favourite pet")
    private var myPetName: StringType? = null

    /**
     * The second example extension uses a List type to provide
     * repeatable values. Note that a [max=] value has been placed in
     * the @Child annotation.
     *
     * Note also that this extension is a modifier extension
     */
    @Child(name = "importantDates", max = Child.MAX_UNLIMITED)
    @Extension(url = "http://example.com/dontuse#importantDates", definedLocally = false, isModifier = true)
    @Description(shortDefinition = "Some dates of note for this patient")
    private var myImportantDates: List<DateTimeType>? = null

    /**
     * It is important to override the isEmpty() method, adding a check for any
     * newly added fields.
     */
    override fun isEmpty(): Boolean {
        return super.isEmpty() && ElementUtil.isEmpty(myPetName, myImportantDates)
    }
    /********
     * Accessors and mutators follow
     *
     * IMPORTANT:
     * Each extension is required to have an getter/accessor and a setter/mutator.
     * You are highly recommended to create getters which create instances if they
     * do not already exist, since this is how the rest of the HAPI FHIR API works.
     */
    /** Getter for important dates  */
    /** Setter for important dates  */
    var importantDates: List<DateTimeType>?
        get() {
            if (myImportantDates == null) {
                myImportantDates = ArrayList()
            }
            return myImportantDates
        }
        set(theImportantDates) {
            myImportantDates = theImportantDates
        }
    /** Getter for pet name  */
    /** Setter for pet name  */
    var petName: StringType?
        get() {
            if (myPetName == null) {
                myPetName = StringType()
            }
            return myPetName
        }
        set(thePetName) {
            myPetName = thePetName
        }

    companion object {
        private const val serialVersionUID = 1L
    }
}
