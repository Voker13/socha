module SurveyorControllerCustomMethods
  def self.included(base)
    # base.send :before_filter, :require_user   # AuthLogic
    # base.send :before_filter, :login_required  # Restful Authentication
     base.send :layout, 'application'
  end

  # Actions
   
  def new
    @survey = @survey_token.survey
    @response_set = @survey_token.response_set ? @survey_token.response_set : ResponseSet.create(:survey => @survey, :user => @current_user, :survey_token => @survey_token) 
    if(@survey && @response_set)
      redirect_to edit_contest_survey_token_survey_url(@contest,@survey_token,:id => 0)
    else
      flash[:error] = "Fehler bei der Teilnahme an dieser Umfrage"
      redirect_to contest_survey_tokens_url(@contest)
    end
  end

  def show
    if @response_set 
      respond_to do |format|
        format.html
        format.csv { send_data(@response_set.to_csv, :type => 'text/csv; charset=utf-8; header=present',:filename => "#{@response_set.updated_at.strftime('%Y-%m-%d')}_#{@response_set.access_code}.csv") }
      end
    else
      flash[:notice] = t('surveyor.unable_to_find_your_responses')
      redirect_to surveyor_index
    end
  end
  
  def edit
    if @response_set 
       @sections = @survey.sections
       if params[:section]
         @section = @sections.find(section_id_from(params[:section])) || @section.first
       else
         @section = @sections.first
       end
       @sections[@sections.size-1].id 
       @dependents = (@response_set.unanswered_dependencies - @section.questions) || []
    else
       flash[:notice] = t('surveyor.unable_to_find_your_responses')
       redirect_to surveyor_index
    end
  end

  def update
    redirect_to contest_survey_tokens_url(@contest) if @response_set.blank?
    saved = false
    ActiveRecord::Base.transaction do 
     saved = @response_set.update_attributes(:responses_attributes => ResponseSet.reject_or_destroy_blanks(params[:r]))
     saved = @response_set.complete! if saved && params[:finish]
     @response_set.save!
    end
    return redirect_with_message(surveyor_finish, :notice, t('surveyor.completed_survey')) if saved && params[:finish]

    respond_to do |format|
      format.html do
          flash[:notice] = t('surveyor.unable_to_update_survey') unless saved
          redirect_to :action => "edit", :anchor => anchor_from(params[:section]), :params => {:section => section_id_from(params[:section])}
        end
        format.js do
          ids, remove, question_ids = {}, {}, []
          ResponseSet.reject_or_destroy_blanks(params[:r]).each do |k,v|
            ids[k] = @response_set.responses.find(:first, :conditions => v).id if !v.has_key?("id")
            remove[k] = v["id"] if v.has_key?("id") && v.has_key?("_destroy")
            question_ids << v["question_id"]
          end
          render :json => {"ids" => ids, "remove" => remove}.merge(@response_set.reload.all_dependencies(question_ids))
        end
      end
  end
  
  # Paths
  def surveyor_index
    contest_survey_tokens_url(@contest)
    # most of the above actions redirect to this method
    #super # available_surveys_path
  end
  def surveyor_finish
    contest_survey_token_url(@contest,@survey_token)
    # the update action redirects to this method if given params[:finish]
    #super # available_surveys_path
  end
end

class SurveyorController < ApplicationController
  helper 'surveyor' 
  I18n.locale = 'de'  
  access_control do 
    allow :administrator
    allow logged_in, :if => :allowed?
  end
  
  before_filter :fetch_survey, :fetch_response_set, :only => [:edit,:update,:show]

  include Surveyor::SurveyorControllerMethods
  include SurveyorControllerCustomMethods

  def allowed?
    @survey_token.allowed_for?(@current_user) and @survey_token.currently_valid?
  end

  def fetch_survey
    @survey = @survey_token.survey
  end

  def fetch_response_set
    @response_set = @survey_token.response_set
  end

end