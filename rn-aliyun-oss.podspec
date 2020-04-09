require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "rn-aliyun-oss"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  rn-aliyun-oss
                   DESC
  s.homepage     = "https://github.com/charway/rn-aliyun-oss"
  # brief license entry:
  s.license      = "MIT"
  # optional - use expanded license entry instead:
  # s.license    = { :type => "MIT", :file => "LICENSE" }
  s.authors      = { "charway" => "charway@qq.com" }
  s.platforms    = { :ios => "9.0" }
  s.source       = { :git => "https://github.com/charway/rn-aliyun-oss.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,swift}"
  s.requires_arc = true

  s.dependency "React"
  s.dependency "AliyunOSSiOS"
  # ...
  # s.dependency "..."
end

