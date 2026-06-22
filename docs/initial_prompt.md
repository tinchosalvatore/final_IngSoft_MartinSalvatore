Hola Claude. Este es un proyecto "de fantasia" para la materia Ing en Informatica de la UM. UM-Book es como un facebook de la
  universidad de mendoza, sobre el cual debo realizar una implementacion super dumb pero que siga estandares (ej no tener user
  hardcodeados, sino una db SQLITE por ej, con Usuarios insertados por nosotros), basicamente no es un proyecto serio real sino una
  implementacion de demo. En esta materia estudiamos el Proceso Unificado de Desarrollo, trabajamos la documentacion de todas las etapas y
  para la de implementacion nos hacen implementar en codigo 1:1 .

  Para el stack tecnologico usa:

  Capa     Tecnología
  Backend     Spring Boot 3.4.3 (Java 21)
  Frontend     Angular 18 (TypeScript)
  Base de datos     MySQL 8.4
  ORM     JPA / Hibernate
  Seguridad     Spring Security + BCrypt
  Comunicación     REST API (JSON)

  respeta estandares de programacion para cada uno, trabaja con entornos virtuales y para el diseño del back tenes el diagrama de clases y
  la secuencia puntual de los CU que tenes que implementar en @docs/diseño/ . En @docs/analisis/diag_colab/  tenes los diagragamas de
  colaboracion para contexto global previo de mis CU

  Veras que dos de ellos son notificacion y uno parte del buscador, por eso planteo lo siguiente:
  1. hacer un home (solo UI) donde lo unico que es real es link al buscador
  2. hacer el buscador (fullstack funcional) aplicando el CU de +2 amigos
  3. hacer un crear usuario simple para poder registrar usuario y su cumpleaños
  4. hacer un script en Java que haga que desde el back se envie una solicitud de amistad para que la UI muestre en vivo la noti de
  amistad
  5. hacer un script en Java que haga que la fecha se setee a la de X usuario para que la UI muestre en vivo la noti de cumple

  Hace el plan y lo reviso. Si lo apruebo comenza por escribir en @docs/ "IMPL_PLAN.md" y luego comenza a hacer los pasos que hayas
  determinado en el plan de a objetivos para poder pushearlos (yo) de a uno.
