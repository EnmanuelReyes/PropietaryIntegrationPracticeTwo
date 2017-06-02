package propietaryintegrationpracticetwo

class Student {
    Integer id
    String name
    String matricula
    String identificacion
    int tipoIdentificacion
    String carrera
    double montoAprobado
    int limiteCredito
    static hasMany = [layouts: Layout]
    static belongsTo = Layout


    static constraints = {
        tipoIdentificacion(inList: [1,2])
        layouts nullable: true
        name nullable: true

    }
}
