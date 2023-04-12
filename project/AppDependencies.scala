import sbt._

object AppDependencies {
  import play.core.PlayVersion
  val bootstrapVersion = "7.15.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"             % "1.1.0",
    "uk.gov.hmrc"       %% "play-frontend-hmrc"             % "6.2.0-play-28",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping"  % "1.13.0-play-28",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"     % bootstrapVersion,
    "uk.gov.hmrc"       %% "domain"                         % "8.2.0-play-28",
    "uk.gov.hmrc"       %% "emailaddress"                   % "3.8.0",
    "org.typelevel"     %% "cats-core"                      % "2.9.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                 %% "bootstrap-test-play-28"   % bootstrapVersion,
    "org.scalatest"               %% "scalatest"                % "3.2.15",
    "org.scalatestplus.play"      %% "scalatestplus-play"       % "5.1.0",
    "uk.gov.hmrc.mongo"           %% "hmrc-mongo-test-play-28"  % "1.1.0",
    "org.scalatestplus"           %% "scalatestplus-scalacheck" % "3.1.0.0-RC2",
    "org.jsoup"                   %  "jsoup"                    % "1.15.4",
    "com.typesafe.play"           %% "play-test"                % PlayVersion.current,
    "org.mockito"                 %% "mockito-scala-scalatest"  % "1.17.14",
    "org.scalacheck"              %% "scalacheck"               % "1.17.0",
    "com.github.tomakehurst"      %  "wiremock-standalone"      % "2.27.2",
    "wolfendale"                  %% "scalacheck-gen-regexp"    % "0.1.2",
    "com.vladsch.flexmark"        %  "flexmark-all"             % "0.64.0"
  ).map(_ % "it, test")

  def apply(): Seq[ModuleID] = compile ++ test

}
