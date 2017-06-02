package propietaryintegrationpracticetwo

class Layout {
    Integer id
    String name
    static hasMany = [students: Student]
    static constraints = {
        name nullable: true
    }
}
