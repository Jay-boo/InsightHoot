class GraphRouter:

  route_app_labels = {"graphs", "feedviewer"}

  def db_for_read(self, model, **hints):
      """
      Attempts to read auth and contenttypes models go to auth_db.
      """
      if model._meta.app_label in self.route_app_labels:
          return "feeds"
      return None

  def db_for_write(self, model, **hints):
      """
      Attempts to read auth and contenttypes models go to auth_db.
      """
      if model._meta.app_label in self.route_app_labels:
          return "feeds"
      return None
