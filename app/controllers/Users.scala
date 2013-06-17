package controllers

import play.api.mvc._
import play.api.data.Forms.{mapping, ignored, nonEmptyText, text, optional, longNumber, boolean}
import models.User
import anorm.{Id, Pk, NotAssigned}
import play.api.data.Form
import jp.t2v.lab.play2.auth.AuthElement
import models.security.{NormalUser, Administrator}
import util.AuthHelper._

object Users extends Controller with AuthElement with AuthConfigImpl {

  def list = StackAction(AuthorityKey -> hasPermission(NormalUser)_) { implicit request =>
    implicit val loggedInUser = Option(loggedIn)
    val usersToList = User.findAll()
    Ok(views.html.users.list(usersToList))
  }

  def show(id : Long) = StackAction(AuthorityKey -> hasPermission(NormalUser)_) { implicit request =>
    implicit val loggedInUser = Option(loggedIn)
    val userToShow = User.find(id)
    Ok(views.html.users.show(userToShow))
  }

  def createForm = StackAction(AuthorityKey -> hasPermission(Administrator)_) { implicit request =>
    implicit val loggedInUser = Option(loggedIn)
    Ok(views.html.users.edit(userCreateForm))
  }

  def create = StackAction(AuthorityKey -> hasPermission(Administrator)_) { implicit request =>
    implicit val loggedInUser = Option(loggedIn)
    userCreateForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.users.edit(formWithErrors)),
      user => {
        User.create(user)
        Redirect(routes.Users.list())
      }
    )
  }

  def updateForm(id: Long) = StackAction(AuthorityKey -> hasPermissionOrSelf(Administrator, userId = id)_) { implicit request =>
    implicit val loggedInUser = Option(loggedIn)
    val userToUpdate = User.find(id)
    Ok(views.html.users.edit(userUpdateForm.fill(userToUpdate), idToUpdate = Option(id)))
  }

  def update(id: Long) = StackAction(AuthorityKey -> hasPermissionOrSelf(Administrator, userId = id)_) { implicit request =>
    implicit val loggedInUser = Option(loggedIn)
    userUpdateForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.users.edit(formWithErrors, Option(id))),
      user => {
        User.update(id, user, updatingPermission = isAdmin(loggedInUser))
        Redirect(routes.Users.show(id))
      }
    )
  }

  def delete(id: Long) = StackAction(AuthorityKey -> hasPermission(Administrator)_) { implicit request =>
    User.delete(id)
    Redirect(routes.Users.list())
  }

  val userCreateForm: Form[User] = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "firstName" -> nonEmptyText(maxLength = 127),
      "lastName" -> nonEmptyText(maxLength = 127),
      "email" -> play.api.data.Forms.email.verifying("Epostadressen används av någon annan", User.findByEmail(_).isEmpty),
      "phone" -> text(maxLength = 127),
      "administrator" -> boolean,
      "password" -> nonEmptyText(minLength = 8, maxLength = 127)
    )(toUser)(fromUser)
  )

  private val primaryKey = optional(longNumber).transform(
    (optionLong: Option[Long]) =>
      if (optionLong.isDefined) {
        Id(optionLong.get)
      } else {
        NotAssigned:Pk[Long]
      },
    (pkLong: Pk[Long]) =>
      pkLong.toOption)


  private val userUpdateForm: Form[User] = Form(
    mapping(
      "id" -> primaryKey,
      "firstName" -> nonEmptyText(maxLength = 127),
      "lastName" -> nonEmptyText(maxLength = 127),
      "email" -> play.api.data.Forms.email,
      "phone" -> text(maxLength = 127),
      "administrator" -> boolean,
      "password" -> nonEmptyText(minLength = 8, maxLength = 127)
    )(toUser)(fromUser)
      .verifying("Epostadressen används av någon annan", user => User.verifyUniqueEmail(user))
  )


  private def toUser(id: Pk[Long], firstName: String, lastName: String, email: String, phone: String, isAdministrator: Boolean, password: String): User = {
    val permission = isAdministrator match {
      case true => Administrator
      case _ => NormalUser
    }
    val passwordToSet = password.trim
    User(id=id, firstName=firstName, lastName=lastName, email=email, phone=phone, permission=permission, password=passwordToSet)
  }

  private def fromUser(user: models.User) = {
    Option(user.id, user.firstName, user.lastName, user.email, user.phone, user.permission==Administrator, User.NOT_CHANGED_PASSWORD)
  }
}

