import styled from 'styled-components';
import BackButton from '../../component/common/BackButton';
import { FaRegEdit } from 'react-icons/fa';
import { BsTrash3 } from 'react-icons/bs';
import { SIZE, COLOR } from '../../style/theme';
import { useState } from 'react';
import CalendarDeleteModal from '../../component/Calendar/CalendarDetailComponent/CalendarDeleteModal';
import { useNavigate } from 'react-router';

const CalendarDetailContainer = styled.div`
  max-width: 1200px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  margin: 0 auto 30px;
`;

// 상세 페이지 헤더
const CalendarDetailHeaderContainer = styled.header`
  width: 100%;
  height: 48px;
  background-color: ${COLOR.main_gray};
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px 0 10px;
  margin-bottom: 30px;

  > p {
    font-size: 20px;
    margin-left: 10px;
    color: ${COLOR.main_dark_blue};
    font-weight: 600;
  }
  @media screen and (min-width: ${SIZE.tablet}) {
  }
`;

// 수정 & 삭제 버튼
const CalendarDetailButtons = styled.div`
  margin-left: auto;
  > :first-child {
    margin-right: 20px;
    cursor: pointer;
  }
  > :last-child {
    cursor: pointer;
  }
`;

// 상세 페이지 바디
const CalendarDetailBodyContainer = styled.section`
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
  > img {
    width: 300px;
    height: 300px;
    border: 1px solid darkcyan;
    margin-bottom: 40px;
  }
`;

// 상세페이지 정보
const CalendarDetailInfoContainer = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
  padding-left: 30px;
  @media screen and (min-width: ${SIZE.tablet}) {
    padding: 0px 30px;
  }
  > p {
    padding-left: 10px;
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 30px;
  }
  > textarea {
    width: 90%;
    min-height: 120px;
    background-color: ${COLOR.bg_light_blue};
    border-radius: 10px;
    padding: 20px 10px 10px 10px;
    :focus {
      outline: none;
    }
    @media screen and (min-width: ${SIZE.tablet}) {
      margin-left: 30px;
    }
  }
`;

const CalendarInfoGroup = styled.div`
  width: 90%;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  padding: 0px 10px 10px 10px;
  border-bottom: 1px solid ${COLOR.main_blue};
  margin-bottom: 40px;
  > p {
    /* margin-right: 30px; */
  }
  > span {
    font-size: 20px;
    font-weight: 400;
  }
  @media screen and (min-width: ${SIZE.tablet}) {
    width: 100%;
    justify-content: start;
    > p {
      margin-right: 40%;
    }
  }
`;

const CalendarDetail = () => {
  const [openDeleteModal, setOpenDeleteModal] = useState(false);

  const handleDeleteModal = () => {
    setOpenDeleteModal(!openDeleteModal);
  };

  const nav = useNavigate();
  const navToEdit = () => {
    nav(`/calendar/:calendarid/edit`);
  };
  return (
    <CalendarDetailContainer>
      <CalendarDetailHeaderContainer>
        <BackButton />
        <p>2023.05.16</p>
        <CalendarDetailButtons>
          <FaRegEdit size={20} onClick={navToEdit} />
          <BsTrash3 size={20} color="red" onClick={handleDeleteModal} />
        </CalendarDetailButtons>
      </CalendarDetailHeaderContainer>
      <CalendarDetailBodyContainer>
        <img src="/" alt="이미지 자리" />
        <CalendarDetailInfoContainer>
          <CalendarInfoGroup>
            <p>장소</p> <span>00수영 센터</span>
          </CalendarInfoGroup>
          <CalendarInfoGroup>
            <p>운동 시간</p> <span>2.5 시간</span>
          </CalendarInfoGroup>
          <p>메모</p>
          <textarea defaultValue={'새 수영복 개시!'} readOnly></textarea>
        </CalendarDetailInfoContainer>
      </CalendarDetailBodyContainer>
      {openDeleteModal ? (
        <CalendarDeleteModal handleDeleteModal={handleDeleteModal} />
      ) : null}
    </CalendarDetailContainer>
  );
};

export default CalendarDetail;