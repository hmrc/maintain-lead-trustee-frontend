import sbt._

object AppDependencies {
  val bootstrapVersion = "7.23.0"
  val mongoVersion = "1.7.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"             % mongoVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc"             % "7.29.0-play-28",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping"  % "1.13.0-play-28",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"     % bootstrapVersion,
    "uk.gov.hmrc"       %% "domain"                         % "8.3.0-play-28",
    "uk.gov.hmrc"       %% "emailaddress"                   % "3.8.0",
    "org.typelevel"     %% "cats-core"                      % "2.10.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                 %% "bootstrap-test-play-28"   % bootstrapVersion,
    "org.scalatest"               %% "scalatest"                % "3.2.17",
    "uk.gov.hmrc.mongo"           %% "hmrc-mongo-test-play-28"  % mongoVersion,
    "org.scalatestplus"           %% "scalacheck-1-17"          % "3.2.17.0",
    "org.jsoup"                   %  "jsoup"                    % "1.17.2",
    "org.mockito"                 %% "mockito-scala-scalatest"  % "1.17.30",
    "org.wiremock"                %  "wiremock-standalone"      % "3.3.1",
    "wolfendale"                  %% "scalacheck-gen-regexp"    % "0.1.2",
    "com.vladsch.flexmark"        %  "flexmark-all"             % "0.64.8"
  ).map(_ % "it, test")

  def apply(): Seq[ModuleID] = compile ++ test

}
