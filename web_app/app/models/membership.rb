class Membership < ActiveRecord::Base

  belongs_to :contestant
  belongs_to :person

  validates_uniqueness_of :contestant_id, :scope => [:person_id]
end
