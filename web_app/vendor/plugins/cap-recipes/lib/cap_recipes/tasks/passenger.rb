Dir[File.join(File.dirname(__FILE__), 'passenger/*.rb')].sort.each { |lib| require lib }